package me.anon.model;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import me.anon.lib.TempUnit;
import me.anon.lib.Unit;

import static me.anon.lib.TempUnit.CELCIUS;
import static me.anon.lib.Unit.ML;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class Water extends Action
{
	private Double ppm;
	private Double ph;
	private Double runoff;
	private Double amount;
	private Double temp;
	private List<Additive> additives = new ArrayList<>();

	@Deprecated private Nutrient nutrient;
	@Deprecated private Double mlpl;

	public void setNutrient(Nutrient nutrient)
	{
		this.nutrient = nutrient;
	}

	public void setMlpl(Double mlpl)
	{
		this.mlpl = mlpl;
	}

	public Nutrient getNutrient()
	{
		return nutrient;
	}

	public Double getMlpl()
	{
		return mlpl;
	}

	public Double getPpm()
	{
		return ppm;
	}

	public void setPpm(Double ppm)
	{
		this.ppm = ppm;
	}

	public Double getPh()
	{
		return ph;
	}

	public void setPh(Double ph)
	{
		this.ph = ph;
	}

	public Double getRunoff()
	{
		return runoff;
	}

	public void setRunoff(Double runoff)
	{
		this.runoff = runoff;
	}

	public Double getAmount()
	{
		return amount;
	}

	public void setAmount(Double amount)
	{
		this.amount = amount;
	}

	public Double getTemp()
	{
		return temp;
	}

	public void setTemp(Double temp)
	{
		this.temp = temp;
	}

	public List<Additive> getAdditives()
	{
		return additives;
	}

	public void setAdditives(List<Additive> additives)
	{
		this.additives = additives;
	}

	public String getSummary(Context context)
	{
		Unit measureUnit = Unit.getSelectedMeasurementUnit(context);
		Unit deliveryUnit = Unit.getSelectedDeliveryUnit(context);
		TempUnit tempUnit = TempUnit.getSelectedTemperatureUnit(context);
		boolean usingEc = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("tds_ec", false);

		String summary = "";
		StringBuilder waterStr = new StringBuilder();

		if (getPh() != null)
		{
			waterStr.append("<b>In pH: </b>");
			waterStr.append(getPh());
			waterStr.append(", ");
		}

		if (getRunoff() != null)
		{
			waterStr.append("<b>Out pH: </b>");
			waterStr.append(getRunoff());
			waterStr.append(", ");
		}

		summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) + "<br/>" : "";

		waterStr = new StringBuilder();

		if (getPpm() != null)
		{
			String ppm = String.valueOf(getPpm().longValue());
			if (usingEc)
			{
				waterStr.append("<b>EC: </b>");
				ppm = String.valueOf((getPpm() * 2d) / 1000d);
			}
			else
			{
				waterStr.append("<b>PPM: </b>");
			}

			waterStr.append(ppm);
			waterStr.append(", ");
		}

		if (getAmount() != null)
		{
			waterStr.append("<b>Amount: </b>");
			waterStr.append(ML.to(deliveryUnit, getAmount()));
			waterStr.append(deliveryUnit.getLabel());
			waterStr.append(", ");
		}

		if (getTemp() != null)
		{
			waterStr.append("<b>Temp: </b>");
			waterStr.append(CELCIUS.to(tempUnit, getTemp()));
			waterStr.append("º").append(tempUnit.getLabel()).append(", ");
		}

		summary += waterStr.toString().length() > 0 ? waterStr.toString().substring(0, waterStr.length() - 2) + "<br/>" : "";

		waterStr = new StringBuilder();

		if (getAdditives().size() > 0)
		{
			waterStr.append("<b>Additives:</b>");

			for (Additive additive : getAdditives())
			{
				if (additive == null || additive.getAmount() == null) continue;

				double converted = ML.to(measureUnit, additive.getAmount());
				String amountStr = converted == Math.floor(converted) ? String.valueOf((int)converted) : String.valueOf(converted);

				waterStr.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;• ");
				waterStr.append(additive.getDescription());
				waterStr.append("  -  ");
				waterStr.append(amountStr);
				waterStr.append(measureUnit.getLabel());
				waterStr.append("/");
				waterStr.append(deliveryUnit.getLabel());
			}
		}

		summary += waterStr.toString();

		return summary;
	}
}
