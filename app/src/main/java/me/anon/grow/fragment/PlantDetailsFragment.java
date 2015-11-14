package me.anon.grow.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kenny.snackbar.SnackBar;
import com.kenny.snackbar.SnackBarListener;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.anon.grow.AddFeedingActivity;
import me.anon.grow.EditFeedingActivity;
import me.anon.grow.EventsActivity;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.grow.StatisticsActivity;
import me.anon.grow.ViewPhotosActivity;
import me.anon.lib.Views;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.task.EncryptTask;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.PlantMedium;
import me.anon.model.PlantStage;
import me.anon.model.StageChange;
import me.anon.model.Water;

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
	@Views.InjectView(R.id.plant_medium) private TextView medium;
	@Views.InjectView(R.id.plant_date) private TextView date;
	@Views.InjectView(R.id.plant_date_container) private View dateContainer;
	@Views.InjectView(R.id.from_clone) private CheckBox clone;

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

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
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

			plant.getActions().add(new StageChange(PlantStage.PLANTED));
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

			if (plant.getMedium() != null)
			{
				medium.setText(plant.getMedium().getPrintString());
			}
		}

		setUi();
	}

	private void setUi()
	{
		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

		String dateStr = dateFormat.format(new Date(plant.getPlantDate())) + " " + timeFormat.format(new Date(plant.getPlantDate()));
		date.setText(dateStr);
		clone.setChecked(plant.isClone());

		dateContainer.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				final DateDialogFragment fragment = new DateDialogFragment(plant.getPlantDate());
				fragment.setOnDateSelected(new DateDialogFragment.OnDateSelectedListener()
				{
					@Override public void onDateSelected(Calendar newDate)
					{
						plant.setPlantDate(newDate.getTimeInMillis());
						String dateStr = dateFormat.format(new Date(plant.getPlantDate())) + " " + timeFormat.format(new Date(plant.getPlantDate()));
						date.setText(dateStr);

						onCancelled();
					}

					@Override public void onCancelled()
					{
						getFragmentManager().beginTransaction().remove(fragment).commit();
					}
				});
				getFragmentManager().beginTransaction().add(fragment, "date").commit();
			}
		});
	}

	@Views.OnClick public void onFeedingClick(final View view)
	{
		Intent feeding = new Intent(view.getContext(), AddFeedingActivity.class);
		feeding.putExtra("plant_index", plantIndex);
		startActivityForResult(feeding, 2);
	}

	@Views.OnClick public void onNoteClick(final View view)
	{
		NoteDialogFragment dialogFragment = new NoteDialogFragment();
		dialogFragment.setOnDialogConfirmed(new NoteDialogFragment.OnDialogConfirmed()
		{
			@Override public void onDialogConfirmed(String notes)
			{
				final NoteAction action = new NoteAction(notes);

				plant.getActions().add(action);
				PlantManager.getInstance().upsert(plantIndex, plant);

				SnackBar.show(getActivity(), "Note added", "undo", new SnackBarListener()
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

	@Views.OnClick public void onPhotoClick(final View view)
	{
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

		File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/GrowTracker/" + plant.getId() + "/");
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
					if (MainApplication.isEncrypted())
					{
						ArrayList<String> image = new ArrayList<>();
						image.add(plant.getImages().get(plant.getImages().size() - 1));
						new EncryptTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
					}

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
				String type = plant.getActions().get(plant.getActions().size() - 1) instanceof Water ? "Watering" : "Feeding";

				SnackBar.show(getActivity(), type + " added", "Apply to another plant", new SnackBarListener()
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
						final ArrayList<Plant> sortedPlants = PlantManager.getInstance().getSortedPlantList();
						CharSequence[] plants = new CharSequence[sortedPlants.size()];
						for (int index = 0; index < plants.length; index++)
						{
							plants[index] = sortedPlants.get(index).getName();
						}

						new AlertDialog.Builder(getActivity())
							.setTitle("Select plant")
							.setItems(plants, new DialogInterface.OnClickListener()
							{
								@Override public void onClick(DialogInterface dialog, int which)
								{
									final int originalIndex = PlantManager.getInstance().getPlants().indexOf(sortedPlants.get(which));

									Water water = (Water)plant.getActions().get(plant.getActions().size() - 1);
									PlantManager.getInstance().getPlants().get(originalIndex).getActions().add(water);

									Intent edit = new Intent(getActivity(), EditFeedingActivity.class);
									edit.putExtra("plant_index", originalIndex);
									edit.putExtra("action_index", PlantManager.getInstance().getPlants().get(originalIndex).getActions().size() - 1);
									startActivityForResult(edit, 2);
								}
							})
							.show();
					}
				});
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		if (plantIndex > -1)
		{
			inflater.inflate(R.menu.plant_menu, menu);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.delete)
		{
			new AlertDialog.Builder(getActivity())
				.setTitle("Are you sure?")
				.setMessage(Html.fromHtml("You are about to delete <b>" + plant.getName() + "</b> and all of the images associated with it, are you sure? This can not be undone."))
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						PlantManager.getInstance().deletePlant(plantIndex);
						PlantManager.getInstance().save();
						getActivity().finish();
					}
				})
				.setNegativeButton("No", null)
				.show();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Views.OnClick public void onActionClick(final View view)
	{
		ActionDialogFragment dialogFragment = new ActionDialogFragment();
		dialogFragment.setOnActionSelected(new ActionDialogFragment.OnActionSelected()
		{
			@Override public void onActionSelected(final EmptyAction action)
			{
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

	@Views.OnClick public void onPlantStageContainerClick(final View view)
	{
		String[] stages = new String[PlantStage.names().length - 1];
		System.arraycopy(PlantStage.names(), 1, stages, 0, stages.length);

		new AlertDialog.Builder(view.getContext())
			.setTitle("Stage")
			.setItems(stages, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					stage.setText(PlantStage.values()[which + 1].getPrintString());
				}
			})
			.show();
	}

	@Views.OnClick public void onPlantMediumContainerClick(final View view)
	{
		String[] mediums = PlantMedium.names();

		new AlertDialog.Builder(view.getContext())
			.setTitle("Medium")
			.setItems(mediums, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					medium.setText(PlantMedium.values()[which].getPrintString());
					plant.setMedium(PlantMedium.values()[which]);
				}
			})
			.show();
	}

	public void save()
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

		PlantStage newStage = PlantStage.valueOf(stage.getText().toString().toUpperCase(Locale.ENGLISH));
		if (plant.getStage() != newStage || (plantIndex < 0 && newStage == PlantStage.GERMINATION))
		{
			plant.getActions().add(new StageChange(newStage));
			plant.setStage(newStage);
		}

		if (plantIndex < 0)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			SharedPreferences.Editor edit = prefs.edit();

			int plantsSize = PlantManager.getInstance().getPlants().size();

			for (int index = 0; index < plantsSize; index++)
			{
				int currentPos = prefs.getInt(String.valueOf(index), 0);

				edit.putInt(String.valueOf(index), currentPos + 1);
			}

			edit.putInt(String.valueOf(plantsSize + 1), 0);
			edit.apply();
		}

		plant.setClone(clone.isChecked());
		PlantManager.getInstance().upsert(plantIndex, plant);
		getActivity().finish();
	}
}
