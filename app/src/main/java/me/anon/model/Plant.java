package me.anon.model;

import java.util.ArrayList;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Accessors(prefix = {"m", ""}, chain = true)
public class Plant
{
	private String name;
	private String strain;
	private long plantDate;
	private PlantStage stage = PlantStage.GERMINATION;
	private ArrayList<String> images;
	private ArrayList<Action> actions;
}
