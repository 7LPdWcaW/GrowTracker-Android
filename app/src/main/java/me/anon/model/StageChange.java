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
public class StageChange
{
	private long date = System.currentTimeMillis();
	private PlantStage newStage;
}
