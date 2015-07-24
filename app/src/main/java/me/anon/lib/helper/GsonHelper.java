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

import java.lang.reflect.Type;

import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.Feed;
import me.anon.model.NoteAction;
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
	public static <T> T parse(String json, Class<T> object)
	{
		return getGson().fromJson(json, object);
	}

	public static <T> T parse(String json, Type object)
	{
		return getGson().fromJson(json, object);
	}

	public static String parse(Object object)
	{
		return getGson().toJson(object);
	}

	public static Gson getGson()
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
					if (json.getAsJsonObject().get("type").getAsString().equals("Feed"))
					{
						action = g.fromJson(jsonObj, Feed.class);
					}
					else if (json.getAsJsonObject().get("type").getAsString().equals("Water"))
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
				}

				return action;
			}
		});

		builder.registerTypeAdapter(Action.class, new JsonSerializer<Action>()
		{
			@Override public JsonElement serialize(Action src, Type typeOfSrc, JsonSerializationContext context)
			{
				if (src instanceof Feed)
				{
					Gson g = new Gson();
					JsonObject jsonObj = (JsonObject)g.toJsonTree(src);
					jsonObj.addProperty("type", "Feed");

					return jsonObj;
				}
				else if (src instanceof Water)
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

				return null;
			}
		});

		return builder.create();
	}
}
