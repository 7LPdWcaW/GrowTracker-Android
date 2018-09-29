package me.anon.model;

import java.util.ArrayList;
import java.util.List;

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
}
