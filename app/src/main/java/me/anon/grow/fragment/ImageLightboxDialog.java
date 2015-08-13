package me.anon.grow.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.text.DateFormat;
import java.util.Date;

import me.anon.grow.R;
import me.anon.lib.DateRenderer;
import me.anon.lib.Views;

@Views.Injectable
public class ImageLightboxDialog extends Activity
{
	private String[] imageUrls = {};

	@Views.InjectView(R.id.pager) public ViewPager pager;
	private int pagerPosition = 0;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_lightbox);

		getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		getWindow().setGravity(Gravity.CENTER);
//		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.fullscreen_dialog_bg));

		if (getIntent().getExtras() != null)
		{
			if (getIntent().getExtras().containsKey("images"))
			{
				imageUrls = getIntent().getExtras().getStringArray("images");
			}

			pagerPosition = getIntent().getExtras().getInt("image_position", 0);
		}
		else
		{
			finish();
			return;
		}

		if (savedInstanceState != null)
		{
			pagerPosition = savedInstanceState.getInt("image_position");
		}

		Views.inject(this);

		pager.setAdapter(new ImagePagerAdapter(imageUrls, pager));
		pager.setCurrentItem(pagerPosition);
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("image_position", pager.getCurrentItem());
		super.onSaveInstanceState(outState);
	}

	private class ImagePagerAdapter extends PagerAdapter
	{
		private String[] images;
		private LayoutInflater inflater;
		private ViewPager pager;

		protected ImagePagerAdapter(String[] images, ViewPager pager)
		{
			this.images = images;
			this.pager = pager;
			inflater = getLayoutInflater();
		}

		@Override public void destroyItem(ViewGroup container, int position, Object object)
		{
			((ViewPager)container).removeView((View)object);
		}

		@Override public void finishUpdate(View container)
		{
		}

		@Override public int getCount()
		{
			return images.length;
		}

		@Override public Object instantiateItem(ViewGroup view, int position)
		{
			final View imageLayout = inflater.inflate(R.layout.image_lightbox_image, view, false);
			loadImage(imageLayout, position);

			try
			{
				DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(view.getContext());
				DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(view.getContext());
				String[] parts = images[position].split("/");
				String date = parts[parts.length - 1].split("\\.")[0];

				String dateStr = dateFormat.format(new Date(Long.parseLong(date))) + " " + timeFormat.format(new Date(Long.parseLong(date)));
				((TextView)imageLayout.findViewById(R.id.taken)).setText(Html.fromHtml("<b>Image taken</b>: " + dateStr + " (" + new DateRenderer().timeAgo(Long.parseLong(date)).formattedDate + " ago)"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			((ViewPager)view).addView(imageLayout, 0);
			return imageLayout;
		}

		private void loadImage(final View v, final int position)
		{
			final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)v.findViewById(R.id.image);
			imageView.setDoubleTapZoomScale(1.5f);
			imageView.setImage(ImageSource.uri("file://" + images[position]));
		}

		@Override public boolean isViewFromObject(View view, Object object)
		{
			return view.equals(object);
		}

		@Override public void restoreState(Parcelable state, ClassLoader loader)
		{
		}

		@Override public Parcelable saveState()
		{
			return null;
		}

		@Override public void startUpdate(View container)
		{
		}
	}
}
