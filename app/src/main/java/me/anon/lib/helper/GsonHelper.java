package me.anon.lib.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Type;

import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.StageChange;
import me.anon.model.Water;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class GsonHelper
{
	private static Gson gson = null;

	public static <T> T parse(String json, Class<T> object)
	{
		return getGson().fromJson(json, object);
	}

	public static <T> T parse(String json, Type object)
	{
		return getGson().fromJson(json, object);
	}

	public static <T> T parse(InputStream json, Type object)
	{
		return getGson().fromJson(new InputStreamReader(json), object);
	}

	public static String parse(Object object)
	{
		return getGson().toJson(object);
	}

	public static void parse(Object object, Writer writer)
	{
		getGson().toJson(object, writer);
	}

	public static Gson getGson()
	{
		if (gson == null)
		{
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Action.class, new JsonDeserializer<Action>()
			{
				@Override public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
				{
					JsonObject jsonObj = json.getAsJsonObject();
					Gson g = new Gson();
					Action action = null;

					if (json.getAsJsonObject().has("type"))
					{
						if (json.getAsJsonObject().get("type").getAsString().equals("Water"))
						{
							action = g.fromJson(jsonObj, Water.class);
						}
						else if (json.getAsJsonObject().get("type").getAsString().equals("Feed"))
						{
							action = g.fromJson(jsonObj, Water.class);
						}
						else if (json.getAsJsonObject().get("type").getAsString().equals("Action"))
						{
							action = g.fromJson(jsonObj, EmptyAction.class);
						}
						else if (json.getAsJsonObject().get("type").getAsString().equals("Note"))
						{
							action = g.fromJson(jsonObj, NoteAction.class);
						}
						else if (json.getAsJsonObject().get("type").getAsString().equals("StageChange"))
						{
							action = g.fromJson(jsonObj, StageChange.class);
						}
					}

					return action;
				}
			});

			builder.registerTypeAdapter(Action.class, new JsonSerializer<Action>()
			{
				@Override public JsonElement serialize(Action src, Type typeOfSrc, JsonSerializationContext context)
				{
					if (src instanceof Water)
					{
						Gson g = new Gson();
						JsonObject jsonObj = (JsonObject)g.toJsonTree(src);
						jsonObj.addProperty("type", "Water");

						return jsonObj;
					}
					else if (src instanceof EmptyAction)
					{
						Gson g = new Gson();
						JsonObject jsonObj = (JsonObject)g.toJsonTree(src);
						jsonObj.addProperty("type", "Action");

						return jsonObj;
					}
					else if (src instanceof NoteAction)
					{
						Gson g = new Gson();
						JsonObject jsonObj = (JsonObject)g.toJsonTree(src);
						jsonObj.addProperty("type", "Note");

						return jsonObj;
					}
					else if (src instanceof StageChange)
					{
						Gson g = new Gson();
						JsonObject jsonObj = (JsonObject)g.toJsonTree(src);
						jsonObj.addProperty("type", "StageChange");

						return jsonObj;
					}

					return null;
				}
			});

			return gson = builder.create();
		}
		else
		{
			return gson;
		}
	}
}
