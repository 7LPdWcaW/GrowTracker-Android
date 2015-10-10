package me.anon.controller.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.grow.fragment.ImageLightboxDialog;
import me.anon.view.ImageHolder;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageHolder>
{
	@Getter private List<String> images = new ArrayList<>();
	@Setter private View.OnLongClickListener onLongClickListener;

	public void setImages(List<String> images)
	{
		this.images.clear();
		this.images.addAll(images);
		Collections.reverse(this.images);
	}

	@Override public ImageHolder onCreateViewHolder(ViewGroup viewGroup, int i)
	{
		return new ImageHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_item, viewGroup, false));
	}

	@Override public void onBindViewHolder(ImageHolder viewHolder, final int i)
	{
		final String imageUri = images.get(i);

		ImageLoader.getInstance().cancelDisplayTask(viewHolder.getImage());
		ImageLoader.getInstance().displayImage("file://" + imageUri, viewHolder.getImage(), MainApplication.getDisplayImageOptions());

		viewHolder.itemView.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				Intent details = new Intent(v.getContext(), ImageLightboxDialog.class);
				details.putExtra("images", (String[])images.toArray(new String[getItemCount()]));
				details.putExtra("image_position", i);
				v.getContext().startActivity(details);
			}
		});

		if (onLongClickListener != null)
		{
			viewHolder.itemView.setOnLongClickListener(onLongClickListener);
		}
	}

	@Override public int getItemCount()
	{
		return images.size();
	}
}
