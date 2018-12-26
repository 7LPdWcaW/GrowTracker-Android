package me.anon.grow.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import me.anon.controller.adapter.ImageAdapter;
import me.anon.controller.adapter.SectionedGridRecyclerViewAdapter;
import me.anon.grow.BuildConfig;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.helper.AddonHelper;
import me.anon.lib.helper.ExportHelper;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.helper.PermissionHelper;
import me.anon.lib.manager.FileManager;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.task.EncryptTask;
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
											plant.getImages().remove(image);
											AddonHelper.broadcastImage(getActivity(), image, true);
										}

										PlantManager.getInstance().upsert(plantIndex, plant);
										setAdapter();
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
					}
				});

				return true;
			}
		});

		recycler.setHasFixedSize(true);

		if (MainApplication.isTablet())
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
		adapter.setImages(PlantManager.getInstance().getPlants().get(plantIndex).getImages());

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
				sections.add(new SectionedGridRecyclerViewAdapter.Section(index, printedFileDate));
			}
		}

		SectionedGridRecyclerViewAdapter sectionedAdapter = new SectionedGridRecyclerViewAdapter(getActivity(), R.layout.section, R.id.section_text, recycler, adapter);
		sectionedAdapter.setSections(sections.toArray(new SectionedGridRecyclerViewAdapter.Section[sections.size()]));

		recycler.setAdapter(sectionedAdapter);
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
			PermissionHelper.doPermissionCheck(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, "Access to external storage is needed to store photos. No other data is consumed by this app");
			return;
		}

		String[] choices = {"From camera", "From gallery"};

		new AlertDialog.Builder(getActivity())
			.setTitle("Select an option")
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

						startActivityForResult(Intent.createChooser(intent, "Select picture"), 3);
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

			PlantManager.getInstance().upsert(plantIndex, plant);
			AddonHelper.broadcastImage(getActivity(), plant.getImages().get(plant.getImages().size() - 1), false);

			setAdapter();
			adapter.notifyDataSetChanged();
		}
		else if (requestCode == 3) // choose image from gallery
		{
			if (resultCode != Activity.RESULT_CANCELED)
			{
				if (data != null && data.getData() != null)
				{
					Uri selectedUri = data.getData();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					{
						getActivity().grantUriPermission(getActivity().getPackageName(), selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

						final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
						getActivity().getContentResolver().takePersistableUriPermission(selectedUri, takeFlags);
					}

					File path = new File(FileManager.IMAGE_PATH + plant.getId() + "/");
					path.mkdirs();

					try
					{
						new File(path, ".nomedia").createNewFile();
					}
					catch (IOException e){}

					File out = new File(path, System.currentTimeMillis() + ".jpg");

					copyImage(selectedUri, out);

					if (out.exists() && out.length() > 0)
					{
						plant.getImages().add(out.getAbsolutePath());

						PlantManager.getInstance().upsert(plantIndex, plant);
						AddonHelper.broadcastImage(getActivity(), plant.getImages().get(plant.getImages().size() - 1), false);

						setAdapter();
						adapter.notifyDataSetChanged();
					}
					else
					{
						out.delete();
					}
				}
			}
		}

		// both photo options
		if ((requestCode == 1 || requestCode == 3) && resultCode != Activity.RESULT_CANCELED)
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
	}

	public void copyImage(Uri imageUri, File newLocation)
	{
		try
		{
			if (imageUri.getScheme().startsWith("content"))
			{
				if (!newLocation.exists())
				{
					newLocation.createNewFile();
				}

				ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(imageUri, "r");
				FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();


				InputStream streamIn = new BufferedInputStream(new FileInputStream(fileDescriptor), 524288);
				OutputStream streamOut = new BufferedOutputStream(new FileOutputStream(newLocation), 524288);

				int len = 0;
				byte[] buffer = new byte[524288];
				while ((len = streamIn.read(buffer)) != -1)
				{
					streamOut.write(buffer, 0, len);
				}

				streamIn.close();
				streamOut.flush();
				streamOut.close();
			}
			else if (imageUri.getScheme().startsWith("file"))
			{
				if (!newLocation.exists())
				{
					newLocation.createNewFile();
				}

				String image = imageUri.getPath();

				InputStream streamIn = new BufferedInputStream(new FileInputStream(new File(image)), 524288);
				OutputStream streamOut = new BufferedOutputStream(new FileOutputStream(newLocation), 524288);

				int len = 0;
				byte[] buffer = new byte[524288];
				while ((len = streamIn.read(buffer)) != -1)
				{
					streamOut.write(buffer, 0, len);
				}

				streamIn.close();
				streamOut.flush();
				streamOut.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
