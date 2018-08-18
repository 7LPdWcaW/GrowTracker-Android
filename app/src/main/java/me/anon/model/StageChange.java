package me.anon.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Getter @Setter
@Accessors(prefix = {"m", ""}, chain = true)
@NoArgsConstructor
public class StageChange extends Action
{
	private PlantStage newStage;

	public StageChange(PlantStage stage)
	{
		this.setDate(System.currentTimeMillis());
		this.setNewStage(stage);
	}
}
