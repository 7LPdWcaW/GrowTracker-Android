package me.anon.model;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class StageChange extends Action
{
	private PlantStage newStage;

	public StageChange()
	{
	}

	public StageChange(PlantStage stage)
	{
		this.setDate(System.currentTimeMillis());
		this.setNewStage(stage);
	}

	public PlantStage getNewStage()
	{
		return newStage;
	}

	public void setNewStage(PlantStage newStage)
	{
		this.newStage = newStage;
	}
}
