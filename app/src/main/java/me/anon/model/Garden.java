package me.anon.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import kotlinx.android.parcel.Parcelize;

@SuppressLint("ParcelCreator")
@Parcelize
public class Garden implements Parcelable
{
	protected String name;
	protected ArrayList<String> plantIds;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ArrayList<String> getPlantIds()
	{
		return plantIds;
	}

	public void setPlantIds(ArrayList<String> plantIds)
	{
		this.plantIds = plantIds;
	}

	@Override public int describeContents()
	{
		return 0;
	}

	@Override public void writeToParcel(Parcel parcel, int i)
	{

	}
}
