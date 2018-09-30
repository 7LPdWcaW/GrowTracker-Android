package me.anon.view;

import android.content.Intent;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;

import me.anon.controller.adapter.ActionAdapter;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.grow.fragment.ImageLightboxDialog;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class ImageActionHolder extends RecyclerView.ViewHolder
{
	private FlexboxLayout flexboxLayout;
	private TextView dateDay;
	private TextView stageDay;
	private ActionAdapter adapter;

	public TextView getDateDay()
	{
		return dateDay;
	}

	public TextView getStageDay()
	{
		return stageDay;
	}

	public ImageActionHolder(ActionAdapter adapter, View itemView)
	{
		super(itemView);

		this.adapter = adapter;
		flexboxLayout = (FlexboxLayout)itemView.findViewById(R.id.image_container);
		dateDay = (TextView)itemView.findViewById(R.id.date_day);
		stageDay = (TextView)itemView.findViewById(R.id.stage_day);
	}

	public void bind(final ArrayList<String> imageUrls)
	{
		flexboxLayout.removeAllViews();
		for (final String imageUrl : imageUrls)
		{
			CardView view = (CardView)LayoutInflater.from(itemView.getContext()).inflate(R.layout.action_image_item, flexboxLayout, false);
			final ImageView image = (ImageView)view.getChildAt(0);

			ImageLoader.getInstance().cancelDisplayTask(image);

			image.post(new Runnable()
			{
				@Override public void run()
				{
					if (image != null && ViewCompat.isLaidOut(image))
					{
						ImageAware imageAware = new ImageViewAware(image, true);
						ImageLoader.getInstance().displayImage("file://" + imageUrl, imageAware, MainApplication.getDisplayImageOptions());
					}
				}
			});

			flexboxLayout.addView(view);

			image.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					Intent details = new Intent(v.getContext(), ImageLightboxDialog.class);
					details.putExtra("images", (String[])adapter.getPlant().getImages().toArray(new String[adapter.getPlant().getImages().size()]));
					details.putExtra("image_position", adapter.getPlant().getImages().indexOf(imageUrl));
					v.getContext().startActivity(details);
				}
			});
		}
	}
}
