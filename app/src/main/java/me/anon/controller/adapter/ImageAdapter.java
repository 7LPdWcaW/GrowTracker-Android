package me.anon.controller.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private List<String> images = new ArrayList<>();
	private List<Integer> selected = new ArrayList<>();
	private View.OnLongClickListener onLongClickListener;
	private boolean inActionMode = false;

	public List<String> getImages()
	{
		return images;
	}

	public List<Integer> getSelected()
	{
		return selected;
	}

	public void setOnLongClickListener(View.OnLongClickListener onLongClickListener)
	{
		this.onLongClickListener = onLongClickListener;
	}

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

	@Override public void onBindViewHolder(final ImageHolder viewHolder, final int position)
	{
		final String imageUri = images.get(position);

		ImageLoader.getInstance().cancelDisplayTask(viewHolder.getImage());
		ImageAware imageAware = new ImageViewAware(viewHolder.getImage(), true);
		ImageLoader.getInstance().displayImage("file://" + imageUri, imageAware, MainApplication.getDisplayImageOptions());

		viewHolder.itemView.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				if (!inActionMode)
				{
					Intent details = new Intent(v.getContext(), ImageLightboxDialog.class);
					details.putExtra("images", (String[])images.toArray(new String[getItemCount()]));
					details.putExtra("image_position", position);
					v.getContext().startActivity(details);
				}
				else
				{
					if (selected.contains((Integer)position))
					{
						selected.remove((Integer)position);
						viewHolder.getSelection().setChecked(false);
					}
					else
					{
						selected.add(position);
						viewHolder.getSelection().setChecked(true);
					}
				}
			}
		});

		viewHolder.getSelection().setChecked(selected.contains((Integer)position));
		viewHolder.getSelection().setVisibility(inActionMode ? View.VISIBLE : View.GONE);

		if (onLongClickListener != null && !inActionMode)
		{
			viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener()
			{
				@Override public boolean onLongClick(View v)
				{
					selected.add(position);
					viewHolder.getSelection().setChecked(true);
					return onLongClickListener.onLongClick(v);
				}
			});
		}
	}

	public void setInActionMode(boolean inActionMode)
	{
		this.inActionMode = inActionMode;

		if (!inActionMode)
		{
			selected.clear();
		}
	}

	@Override public int getItemCount()
	{
		return images.size();
	}
}
