package me.anon.grow.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import me.anon.grow.R;
import me.anon.lib.DateRenderer;
import me.anon.lib.Views;
import me.anon.lib.helper.TimeHelper;
import me.anon.model.Action;
import me.anon.model.Plant;
import me.anon.model.StageChange;

@Views.Injectable
public class ImageLightboxDialog extends FragmentActivity
{
	private String[] imageUrls = {};
	private Plant plant;

	@Views.InjectView(R.id.pager) public ViewPager pager;
	private int pagerPosition = 0;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_lightbox);

		getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		getWindow().setGravity(Gravity.CENTER);

		if (getIntent().getExtras() != null)
		{
			if (getIntent().getExtras().containsKey("images"))
			{
				imageUrls = getIntent().getExtras().getStringArray("images");
			}

			plant = getIntent().getParcelableExtra("plant");
			pagerPosition = getIntent().getExtras().getInt("image_position", 0);
		}
		else
		{
			finish();
			return;
		}

		if (savedInstanceState != null)
		{
			plant = savedInstanceState.getParcelable("plant");
			pagerPosition = savedInstanceState.getInt("image_position");
		}

		Views.inject(this);

		pager.setAdapter(new ImagePagerAdapter(imageUrls, pager));
		pager.setCurrentItem(pagerPosition);
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putParcelable("plant", plant);
		outState.putInt("image_position", pager.getCurrentItem());
		super.onSaveInstanceState(outState);
	}

	@Override protected void onDestroy()
	{
		super.onDestroy();

		for (int index = 0; index < pager.getChildCount(); index++)
		{
			((ImagePagerAdapter)pager.getAdapter()).destroyItem(null, (int)pager.getChildAt(index).getTag(), (ViewGroup)pager.getChildAt(index));
		}
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
			if (object != null)
			{
				try
				{
					ExifInterface exifInterface = new ExifInterface(images[position]);
					exifInterface.setAttribute("UserComment", ((EditText)((View)object).findViewById(R.id.comment)).getText().toString());
					exifInterface.saveAttributes();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				if (container != null)
				{
					forceHideKeyboard(container);

					((ViewPager)container).removeView((View)object);
				}
			}
		}

		@Override public void finishUpdate(View container)
		{
		}

		@Override public int getCount()
		{
			return images.length;
		}

		private void forceHideKeyboard(ViewGroup view)
		{
			for (int index = 0; index < view.getChildCount(); index++)
			{
				EditText v = (EditText)view.getChildAt(index).findViewById(R.id.comment);

				if (v != null)
				{
					InputMethodManager m = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					m.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		}

		@Override public Object instantiateItem(ViewGroup view, final int position)
		{
			forceHideKeyboard(view);

			final View imageLayout = inflater.inflate(R.layout.image_lightbox_image, view, false);
			imageLayout.setTag(position);
			loadImage(imageLayout, position);

			try
			{
				DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(view.getContext());
				DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(view.getContext());
				String[] parts = images[position].split("/");
				String fileDate = parts[parts.length - 1].split("\\.")[0];
				Date date = new Date(Long.parseLong(fileDate));

				StageChange lastChange = null;
				long currentChangeDate = date.getTime();

				for (int index = plant.getActions().size() - 1; index >= 0; index--)
				{
					Action action = plant.getActions().get(index);
					if (action instanceof StageChange)
					{
						if (action.getDate() < date.getTime() && lastChange == null)
						{
							lastChange = (StageChange)action;
							break;
						}
					}
				}

				String stageDayStr = "";
				if (lastChange != null)
				{
					stageDayStr = " – ";
					int totalDays = (int)TimeHelper.toDays(Math.abs(date.getTime() - plant.getPlantDate()));
					stageDayStr += (totalDays == 0 ? 1 : totalDays);

					int currentDays = (int)TimeHelper.toDays(Math.abs(currentChangeDate - lastChange.getDate()));
					currentDays = (currentDays == 0 ? 1 : currentDays);
					stageDayStr += "/" + currentDays + getString(lastChange.getNewStage().getPrintString()).substring(0, 1).toLowerCase();
				}

				String dateStr = dateFormat.format(date) + " " + timeFormat.format(date);
				((TextView)imageLayout.findViewById(R.id.taken)).setText(Html.fromHtml("<b>" + getString(R.string.taken) + "</b>: " + dateStr + stageDayStr + " (" + getString(R.string.ago,  new DateRenderer().timeAgo(date.getTime()).formattedDate) + ")"));

				try
				{
					ExifInterface exifInterface = new ExifInterface(images[position]);
					String comments = exifInterface.getAttribute("UserComment");

					((ImageView)imageLayout.findViewById(R.id.comment_icon)).setImageResource(!TextUtils.isEmpty(comments) ? R.drawable.ic_comment : R.drawable.ic_empty_comment);
					((EditText)imageLayout.findViewById(R.id.comment)).setText(comments);
					((EditText)imageLayout.findViewById(R.id.comment)).setOnEditorActionListener(new TextView.OnEditorActionListener()
					{
						public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
						{
							if (actionId == EditorInfo.IME_ACTION_DONE)
							{
								InputMethodManager m = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
								m.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
								imageLayout.findViewById(R.id.comment_icon).callOnClick();
								return true;
							}

							return false;
						}
					});

					imageLayout.findViewById(R.id.comment_icon).setOnClickListener(new View.OnClickListener()
					{
						@Override public void onClick(View v)
						{
							int visibility = imageLayout.findViewById(R.id.comment).getVisibility();
							imageLayout.findViewById(R.id.comment).setVisibility(visibility == View.GONE ? View.VISIBLE : View.GONE);

							if (visibility == View.GONE)
							{
								imageLayout.findViewById(R.id.comment).requestFocus();
							}
						}
					});
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
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

			ImageLoader.getInstance().loadImage("file://" + images[position], new SimpleImageLoadingListener()
			{
				@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
				{
					imageView.setImage(ImageSource.bitmap(loadedImage));
				}
			});
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
