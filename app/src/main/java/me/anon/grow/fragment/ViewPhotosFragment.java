package me.anon.grow.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kenny.snackbar.SnackBar;
import com.kenny.snackbar.SnackBarListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
		adapter.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override public boolean onLongClick(View v)
			{
				((AppCompatActivity)getActivity()).startSupportActionMode(new ActionMode.Callback()
				{
					@Override public boolean onCreateActionMode(ActionMode mode, Menu menu)
					{
						getActivity().getMenuInflater().inflate(R.menu.photo_menu, menu);
						adapter.setInActionMode(true);
						adapter.notifyDataSetChanged();

						return true;
					}

					@Override public boolean onPrepareActionMode(ActionMode mode, Menu menu)
					{
						return false;
					}

					@Override public boolean onActionItemClicked(final ActionMode mode, MenuItem item)
					{
						if (item.getItemId() == R.id.delete)
						{
							new AlertDialog.Builder(getActivity())
								.setTitle("Are you sure?")
								.setMessage("You're about to delete " + adapter.getSelected().size() + " images, are you sure? You will not be able to recover these")
								.setPositiveButton("Yes", new DialogInterface.OnClickListener()
								{
									@Override public void onClick(DialogInterface dialog, int which)
									{
										for (Integer integer : adapter.getSelected())
										{
											String image = adapter.getImages().get(integer);
											PlantManager.getInstance().getPlants().get(plantIndex).getImages().remove(image);
										}

										adapter.setImages(PlantManager.getInstance().getPlants().get(plantIndex).getImages());
										adapter.notifyDataSetChanged();
										mode.finish();
									}
								})
								.setNegativeButton("No", null)
								.show();

							return true;
						}
						else if (item.getItemId() == R.id.share)
						{
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_SEND_MULTIPLE);
							intent.setType("image/jpeg");

							ArrayList<Uri> files = new ArrayList<Uri>();

							for (Integer integer : adapter.getSelected())
							{
								String image = adapter.getImages().get(integer);
								File file = new File(image);
								Uri uri = Uri.fromFile(file);
								files.add(uri);
							}

							intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
							startActivity(intent);

							return true;
						}

						return false;
					}

					@Override public void onDestroyActionMode(ActionMode mode)
					{
						adapter.setInActionMode(false);
						adapter.notifyDataSetChanged();
					}
				});

				return true;
			}
		});
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
