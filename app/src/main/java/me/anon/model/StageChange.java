package me.anon.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Accessors(prefix = {"m", ""}, chain = true)
public class StageChange extends Action
{
	private PlantStage newStage;

	public StageChange(PlantStage stage)
	{
		this.setDate(System.currentTimeMillis());
		this.setNewStage(stage);
	}
}
