package me.anon.grow.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.Pair;
import me.anon.controller.adapter.ImageAdapter;
import me.anon.controller.adapter.SectionedGridRecyclerViewAdapter;
import me.anon.grow.BuildConfig;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.SnackBar;
import me.anon.lib.SnackBarListener;
import me.anon.lib.Views;
import me.anon.lib.helper.AddonHelper;
import me.anon.lib.helper.ExportHelper;
import me.anon.lib.helper.NotificationHelper;
import me.anon.lib.helper.PermissionHelper;
import me.anon.lib.helper.TimeHelper;
import me.anon.lib.manager.FileManager;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.task.AsyncCallback;
import me.anon.lib.task.EncryptTask;
import me.anon.lib.task.ImportTask;
import me.anon.model.Action;
import me.anon.model.Plant;
import me.anon.model.StageChange;

@Views.Injectable
public class ViewPhotosFragment extends Fragment
{
	private ImageAdapter adapter;

	@Views.InjectView(R.id.recycler_view) private RecyclerView recycler;
	@Views.InjectView(R.id.empty) private View empty;
	private ActionMode action = null;

	private Plant plant;

	public static ViewPhotosFragment newInstance(Bundle arguments)
	{
		ViewPhotosFragment fragment = new ViewPhotosFragment();
		fragment.setArguments(arguments);

		return fragment;
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.image_grid_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState)
	{
		outState.putParcelable("plant", plant);
		super.onSaveInstanceState(outState);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		getActivity().setTitle(R.string.photos_title);

		if (getArguments() != null)
		{
			plant = getArguments().getParcelable("plant");
		}
		else if (savedInstanceState != null)
		{
			plant = savedInstanceState.getParcelable("plant");
		}

		if (plant == null)
		{
			getActivity().finish();
			return;
		}

		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		adapter = new ImageAdapter();
		adapter.plant = plant;
		adapter.onItemSelectedListener = new ImageAdapter.OnItemSelectedListener()
		{
			@Override public void onItemSelected(int totalSelected)
			{
				if (action == null) return;

				if (totalSelected == 0)
				{
					action.finish();
				}
				else
				{
					action.setTitle(getString(R.string.selected_len, totalSelected));
				}
			}
		};
		adapter.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override public boolean onLongClick(View v)
			{
				Toolbar toolbar = ((AppCompatActivity)getActivity()).findViewById(R.id.toolbar);
				action = toolbar.startActionMode(new ActionMode.Callback()
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
								.setTitle(R.string.confirm_title)
								.setMessage(getString(R.string.confirm_delete_photos_message, "" + adapter.getSelected().size()))
								.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
								{
									@Override public void onClick(DialogInterface dialog, int which)
									{
										for (Integer integer : adapter.getSelected())
										{
											String image = adapter.getImages().get(integer);
											new File(image).delete();
											plant.getImages().remove(image);
											AddonHelper.broadcastImage(getActivity(), image, true);
										}

										PlantManager.getInstance().upsert(plant);
										setAdapter();
										adapter.notifyDataSetChanged();
										setEmpty();
										mode.finish();

										Intent intent = new Intent();
										intent.putExtra("plant", plant);
										getActivity().setIntent(intent);
										getActivity().setResult(Activity.RESULT_OK, intent);
									}
								})
								.setNegativeButton(R.string.no, null)
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
								Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
								files.add(uri);
							}

							intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
							intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
							startActivity(Intent.createChooser(intent, "Share with"));

							return true;
						}

						return false;
					}

					@Override public void onDestroyActionMode(ActionMode mode)
					{
						adapter.setInActionMode(false);
						adapter.notifyDataSetChanged();
						action = null;
					}
				});
				action.setTitle(getString(R.string.selected_len, 1));

				return true;
			}
		});

		recycler.setHasFixedSize(true);

		if (MainApplication.isTablet() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			recycler.setLayoutManager(new GridLayoutManager(getActivity(), 6));
		}
		else
		{
			recycler.setLayoutManager(new GridLayoutManager(getActivity(), 3));
		}

		setAdapter();
	}

	private void setAdapter()
	{
		adapter.setImages(plant.getImages());

		String lastFileDate = "";
		List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<SectionedGridRecyclerViewAdapter.Section>();
		for (int index = 0, count = adapter.getImages().size(); index < count; index++)
		{
			String image = adapter.getImages().get(index);

			File currentImage = new File(image);
			long fileDate = Long.parseLong(currentImage.getName().replaceAll("[^0-9]", ""));

			if (fileDate == 0)
			{
				fileDate = currentImage.lastModified();
			}

			String printedFileDate = ExportHelper.dateFolder(getActivity(), fileDate);
			if (!lastFileDate.equalsIgnoreCase(printedFileDate))
			{
				lastFileDate = printedFileDate;

				StageChange lastChange = null;
				StageChange currentChange = new StageChange();
				currentChange.setDate(fileDate);

				for (int actionIndex = plant.getActions().size() - 1; actionIndex >= 0; actionIndex--)
				{
					Action action = plant.getActions().get(actionIndex);
					if (action instanceof StageChange)
					{
						if (action.getDate() < fileDate && lastChange == null)
						{
							lastChange = (StageChange)action;
							break;
						}
					}
				}

				String stageDayStr = " – ";
				if (lastChange != null)
				{
					stageDayStr = " – ";
					int totalDays = (int)TimeHelper.toDays(Math.abs(fileDate - plant.getPlantDate()));
					stageDayStr += (totalDays == 0 ? 1 : totalDays);

					int currentDays = (int)TimeHelper.toDays(Math.abs(currentChange.getDate() - lastChange.getDate()));
					currentDays = (currentDays == 0 ? 1 : currentDays);
					stageDayStr += "/" + currentDays + getString(lastChange.getNewStage().getPrintString()).substring(0, 1).toLowerCase();
				}

				sections.add(new SectionedGridRecyclerViewAdapter.Section(index, printedFileDate + stageDayStr));
			}
		}

		SectionedGridRecyclerViewAdapter sectionedAdapter = new SectionedGridRecyclerViewAdapter(getActivity(), R.layout.section, R.id.section_text, recycler, adapter);
		sectionedAdapter.setSections(sections.toArray(new SectionedGridRecyclerViewAdapter.Section[sections.size()]));

		recycler.setAdapter(sectionedAdapter);

		setEmpty();
	}

	private void setEmpty()
	{
		if (adapter.getItemCount() == 0)
		{
			empty.setVisibility(View.VISIBLE);
			recycler.setVisibility(View.GONE);
		}
		else
		{
			empty.setVisibility(View.GONE);
			recycler.setVisibility(View.VISIBLE);
		}
	}

	@Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (requestCode == 1 && (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
		{
			onFabPhotoClick(null);
		}
	}

	@Views.OnClick public void onFabPhotoClick(final View view)
	{
		if (!PermissionHelper.hasPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
		{
			PermissionHelper.doPermissionCheck(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, getString(R.string.permission_summary));
			return;
		}

		String[] choices = {getString(R.string.photo_option_camera), getString(R.string.photo_option_gallery)};

		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.dialog_option_title)
			.setItems(choices, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialogInterface, int index)
				{
					if (index == 0)
					{
						Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

						File path = new File(FileManager.IMAGE_PATH + plant.getId() + "/");
						path.mkdirs();

						try
						{
							new File(path, ".nomedia").createNewFile();
						}
						catch (IOException e){}

						File out = new File(path, System.currentTimeMillis() + ".jpg");
						Uri photoURI = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", out);

						plant.getImages().add(out.getAbsolutePath());

						intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
						startActivityForResult(intent, 1);
					}
					else
					{
						if (!PermissionHelper.hasPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
						{
							PermissionHelper.doPermissionCheck(ViewPhotosFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, "Need permission");
							return;
						}

						Intent intent;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
						{
							intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
							intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
							intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
						}
						else
						{
							intent = new Intent(Intent.ACTION_GET_CONTENT);
						}

						intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
						intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						intent.setType("image/*");

						startActivityForResult(Intent.createChooser(intent, getString(R.string.dialog_select_picture_title)), 3);
					}
				}
			})
			.show();
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 1)
		{
			if (resultCode == Activity.RESULT_CANCELED)
			{
				new File(plant.getImages().get(plant.getImages().size() - 1)).delete();
				plant.getImages().remove(plant.getImages().size() - 1);
			}
			else
			{
				PlantManager.getInstance().upsert(plant);
				AddonHelper.broadcastImage(getActivity(), plant.getImages().get(plant.getImages().size() - 1), false);

				setAdapter();
				adapter.notifyDataSetChanged();
			}
		}
		else if (requestCode == 3) // choose image from gallery
		{
			if (resultCode != Activity.RESULT_CANCELED)
			{
				if (data == null) return;

				ArrayList<Uri> images = new ArrayList<>();
				if (data.getData() != null)
				{
					images.add(data.getData());
				}
				else if (data.getClipData() != null)
				{
					for (int index = 0; index < data.getClipData().getItemCount(); index++)
					{
						images.add(data.getClipData().getItemAt(index).getUri());
					}
				}
				images.removeAll(Collections.singleton(null));

				for (Uri image : images)
				{
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					{
						getActivity().grantUriPermission(getActivity().getPackageName(), image, Intent.FLAG_GRANT_READ_URI_PERMISSION);

						final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
						getActivity().getContentResolver().takePersistableUriPermission(image, takeFlags);
					}
				}

				NotificationHelper.sendDataTaskNotification(getActivity(), getString(R.string.app_name), getString(R.string.import_progress_warning));
				new ImportTask(getActivity(), new AsyncCallback()
				{
					@Override public void callback()
					{
						if (getActivity() != null && !getActivity().isFinishing())
						{
							setAdapter();
							adapter.notifyDataSetChanged();
						}
					}
				}).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Pair<>(plant.getId(), images));
			}
		}

		// both photo options
		setEmpty();
		if ((requestCode == 1 || requestCode == 3) && resultCode != Activity.RESULT_CANCELED)
		{
			Intent intent = new Intent();
			intent.putExtra("plant", plant);
			getActivity().setIntent(intent);
			getActivity().setResult(Activity.RESULT_OK, intent);

			if (getActivity() != null)
			{
				if (MainApplication.isEncrypted())
				{
					ArrayList<String> image = new ArrayList<>();
					image.add(plant.getImages().get(plant.getImages().size() - 1));
					new EncryptTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
				}

				SnackBar.show(getActivity(), R.string.snackbar_image_added, R.string.snackbar_action_take_another, new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o){}
					@Override public void onSnackBarFinished(Object o){}

					@Override public void onSnackBarAction(View v)
					{
						onFabPhotoClick(null);
					}
				});
			}
		}
	}
}
