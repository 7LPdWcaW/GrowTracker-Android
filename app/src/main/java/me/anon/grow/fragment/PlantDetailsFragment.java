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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.kenny.snackbar.SnackBar;
import com.kenny.snackbar.SnackBarListener;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import me.anon.grow.AddFeedingActivity;
import me.anon.grow.EventsActivity;
import me.anon.grow.R;
import me.anon.grow.StatisticsActivity;
import me.anon.grow.ViewPhotosActivity;
import me.anon.lib.Views;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.Plant;
import me.anon.model.PlantStage;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class PlantDetailsFragment extends Fragment
{
	@Views.InjectView(R.id.action_container) private View actionContainer;
	@Views.InjectView(R.id.link_container) private View linkContainer;

	@Views.InjectView(R.id.plant_name) private TextView name;
	@Views.InjectView(R.id.plant_strain) private TextView strain;
	@Views.InjectView(R.id.plant_stage) private TextView stage;

	private int plantIndex = -1;
	private Plant plant;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static PlantDetailsFragment newInstance(int plantIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);

		PlantDetailsFragment fragment = new PlantDetailsFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.plant_details_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

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
			plant = new Plant();
			getActivity().setTitle("Add new plant");
		}
		else
		{
			getActivity().setTitle("Plant details");

			actionContainer.setVisibility(View.VISIBLE);
			linkContainer.setVisibility(View.VISIBLE);

			name.setText(plant.getName());
			strain.setText(plant.getStrain());

			if (plant.getStage() != null)
			{
				stage.setText(plant.getStage().getPrintString());
			}
		}
	}

	@Views.OnClick public void onFeedingClick(final View view)
	{
		Intent feeding = new Intent(view.getContext(), AddFeedingActivity.class);
		feeding.putExtra("plant_index", plantIndex);
		startActivityForResult(feeding, 2);
	}

	@Views.OnClick public void onPhotoClick(final View view)
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
								FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
							}
						}

						@Override public void onSnackBarFinished(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
							}
						}

						@Override public void onSnackBarAction(Object o)
						{
							onPhotoClick(null);
						}
					});
				}
			}

			PlantManager.getInstance().upsert(plantIndex, plant);
		}
		else if (requestCode == 2)
		{
			if (resultCode != Activity.RESULT_CANCELED)
			{
				PlantManager.getInstance().upsert(plantIndex, plant);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Views.OnClick public void onActionClick(final View view)
	{
		ActionDialogFragment dialogFragment = new ActionDialogFragment();
		dialogFragment.setOnActionSelected(new ActionDialogFragment.OnActionSelected()
		{
			@Override public void onActionSelected(Action.ActionName actionName, String notes)
			{
				final EmptyAction action = new EmptyAction(actionName);

				if (notes != null)
				{
					action.setNotes(notes);
				}

				plant.getActions().add(action);
				PlantManager.getInstance().upsert(plantIndex, plant);

				SnackBar.show(getActivity(), action.getAction().getPrintString() + " added", "undo", new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
						}
					}

					@Override public void onSnackBarFinished(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
						}
					}

					@Override public void onSnackBarAction(Object o)
					{
						plant.getActions().remove(action);
						PlantManager.getInstance().upsert(plantIndex, plant);
					}
				});
			}
		});
		dialogFragment.show(getFragmentManager(), null);
	}

	@Views.OnClick public void onViewStatisticsClick(View view)
	{
		Intent stats = new Intent(getActivity(), StatisticsActivity.class);
		stats.putExtra("plant_index", plantIndex);
		startActivity(stats);
	}

	@Views.OnClick public void onViewHistoryClick(View view)
	{
		Intent events = new Intent(getActivity(), EventsActivity.class);
		events.putExtra("plant_index", plantIndex);
		startActivity(events);
	}

	@Views.OnClick public void onViewPhotosClick(View view)
	{
		Intent photos = new Intent(getActivity(), ViewPhotosActivity.class);
		photos.putExtra("plant_index", plantIndex);
		startActivity(photos);
	}

	@Views.OnClick public void onPlantStageClick(final View view)
	{
		new AlertDialog.Builder(view.getContext())
			.setTitle("Stage")
			.setItems(PlantStage.names(), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (which == 2)
					{
						plant.getActions().add(new EmptyAction(Action.ActionName.FLIPPED));
					}

					((TextView)view).setText(PlantStage.values()[which].getPrintString());
				}
			})
			.show();
	}

	@Views.OnClick public void onFabCompleteClick(final View view)
	{
		name.setError(null);
		strain.setError(null);

		if (!TextUtils.isEmpty(name.getText()))
		{
			plant.setName(name.getText().toString().trim());
		}
		else
		{
			name.setError("Name can not be empty");
			return;
		}

		if (!TextUtils.isEmpty(strain.getText()))
		{
			plant.setStrain(strain.getText().toString().trim());
		}
		else
		{
			strain.setError("strain can not be empty");
			return;
		}

		plant.setStage(PlantStage.valueOf(stage.getText().toString().toUpperCase(Locale.ENGLISH)));

		PlantManager.getInstance().upsert(plantIndex, plant);
		getActivity().finish();
	}
}
