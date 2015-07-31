package me.anon.grow.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kenny.snackbar.SnackBar;
import com.kenny.snackbar.SnackBarListener;

import java.io.File;
import java.io.IOException;

import me.anon.controller.adapter.ImageAdapter;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class ViewPhotosFragment extends Fragment
{
	private ImageAdapter adapter;

	@Views.InjectView(R.id.recycler_view) private RecyclerView recycler;

	private int plantIndex = -1;
	private Plant plant;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static ViewPhotosFragment newInstance(int plantIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);

		ViewPhotosFragment fragment = new ViewPhotosFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.image_grid_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		getActivity().setTitle("Plant photos");

		if (getArguments() != null)
		{
			plantIndex = getArguments().getInt("plant_index");

			if (plantIndex > -1)
			{
				plant = PlantManager.getInstance().getPlants().get(plantIndex);
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		}

		if (plant == null)
		{
			getActivity().finish();
			return;
		}

		adapter = new ImageAdapter();
		adapter.setImages(PlantManager.getInstance().getPlants().get(plantIndex).getImages());
		recycler.setLayoutManager(new GridLayoutManager(getActivity(), 3));
		recycler.setAdapter(adapter);
	}

	@Views.OnClick public void onFabPhotoClick(final View view)
	{
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

		File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/GrowTracker/" + plant.getName() + "/");
		path.mkdirs();

		try
		{
			new File(path, ".nomedia").createNewFile();
		}
		catch (IOException e){}

		File out = new File(path, System.currentTimeMillis() + ".jpg");

		plant.getImages().add(out.getAbsolutePath());

		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(out));
		startActivityForResult(intent, 1);
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 1)
		{
			if (resultCode == Activity.RESULT_CANCELED)
			{
				plant.getImages().remove(plant.getImages().size() - 1);
			}
			else
			{
				if (getActivity() != null)
				{
					SnackBar.show(getActivity(), "Image added", "Take another", new SnackBarListener()
					{
						@Override public void onSnackBarStarted(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateUp(getView().findViewById(R.id.fab_photo));
							}
						}

						@Override public void onSnackBarFinished(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateDown(getView().findViewById(R.id.fab_photo));
							}
						}

						@Override public void onSnackBarAction(Object o)
						{
							onFabPhotoClick(null);
						}
					});
				}
			}

			PlantManager.getInstance().upsert(plantIndex, plant);

			adapter.setImages(PlantManager.getInstance().getPlants().get(plantIndex).getImages());
			adapter.notifyDataSetChanged();
		}
	}
}
