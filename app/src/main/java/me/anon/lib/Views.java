package me.anon.lib;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build.VERSION;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.anon.grow.R;

/**
 * View injection class which runs at runtime rather than compile time like ButterKnife. The benefit to this is there's no generated code which some IDEs (such as Eclipse) fail to generate properly and can often lead to broken references.
 * Also contains an onClick annotation.
 *
 * <h1>Usage:</h1>
 * <code>
 * // View inject for variable
 * @InjectView(R.id.view_id) private View view;
 * @InjectView(id = "view_id") private View view; // use this in libraries because IDs are not constant
 * @InjectView private View view; // Will use the member name (lower case, underscore before capitals) for the lookup. E.G: myView will search for R.id.my_view
 *
 * // Multiple view inject
 * @InjectViews({R.id.view, R.id.view2}) private List&lt;View&gt; views;
 * @InjectViews(id = {"view", "view2"}) private List&lt;View&gt; views;
 * @InjectViews(instances = {TextView.class, Button.class}) private List&lt;View&gt; views;
 *
 * // Injecting a fragment
 * @InjectFragment(R.id.fragment_holder) private Fragment fragment;
 * @InjectFragment(tag = "fragment_holder") private Fragment fragment;
 *
 * // OnClick for variable
 * @OnClick private View view; // var needs to be initialized *before* calling Views.inject. Uses current class which implements View.OnClickListener
 * @OnClick(method = "methodName") private View view; // method name must exist with 1 parameter for View
 *
 * // OnClick for method
 * @OnClick private void onViewIdClick(View v){} // Method name must match (on)?(.*)Click where .* is the view name capitalised. The prefix "on" is optional. This will be converted to lower case, underscore before capitals
 * @OnClick(id = "view_id") private void onViewIdClick(View v){} // String id. Use this in libraries because IDs are not constant
 * @OnClick(R.id.view_id) private void onViewIdClick(View v){} // Use standard ID reference
 *
 * // Methods work without parameters also
 * @OnClick private void onViewIdClick(){}
 * @OnClick(id = "view_id") private void onViewIdClick(){}
 * @OnClick(R.id.view_id) private void onViewIdClick(){}
 *
 * // Mass view property applying
 * Views.apply(views, ViewProperty.VISIBILITY, View.VISIBLE);
 * Views.apply(views, new Property&lt;TextView, String&gt;()
 * {
 *     public void apply(TextView view, String value)
 *     {
 *     	view.setText(value);
 *     }
 * }, "Some text");
 * </code>
 *
 * To execute the injector service, you must call <code>Views.inject(this);</code> after setting the content.
 * You can also call <code>Views.inject(this, view)</code> in fragments or view holders to populate the members from a base view.
 *
 *
 * <h1>License</h1>
 * <pre>
 * Copyright 2013 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <pre>
 *
 * Finder enum code used from <a href="https://github.com/JakeWharton/butterknife">ButterKnife</a>
 */
public class Views
{
	// For function trailing
	private static final Views instance = new Views();

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	public @interface InjectViews
	{
		int[] value() default {};
		String[] id() default {};
		Class[] instances() default {};
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	public @interface InjectView
	{
		int value() default 0;
		String id() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	public @interface InjectFragment
	{
		int value() default 0;
		String tag() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE})
	public @interface Injectable
	{

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.METHOD})
	public @interface OnClick
	{
		int value() default 0;
		String id() default "";
		String method() default "";
	}

	private enum Finder
	{
		VIEW
		{
			@SuppressWarnings("unchecked") @Override public <T extends View> T findById(Object source, int id)
			{
				return (T)((View)source).findViewById(id);
			}

			@SuppressWarnings("unchecked") @Override public <T extends View> List<T> findByInstance(Object source, Class instanceType)
			{
				return (List<T>)findAllChildrenByClass(((ViewGroup)source), instanceType);
			}

			@Override public Object findFragmentById(Object source, int id)
			{
				Object fragment = null;

				if (source instanceof Fragment && VERSION.SDK_INT >= 11)
				{
					fragment = ((Fragment)source).getFragmentManager().findFragmentById(id);
				}
				else if (source instanceof Fragment)
				{
					fragment = ((Fragment)source).getFragmentManager().findFragmentById(id);
				}

				return fragment;
			}

			@Override public Object findFragmentByTag(Object source, String tag)
			{
				Object fragment = null;

				if (source instanceof Fragment && VERSION.SDK_INT >= 11)
				{
					fragment = ((Fragment)source).getFragmentManager().findFragmentByTag(tag);
				}
				else if (source instanceof Fragment)
				{
					fragment = ((Fragment)source).getFragmentManager().findFragmentByTag(tag);
				}

				return fragment;
			}
		},
		ACTIVITY
		{
			@SuppressWarnings("unchecked") @Override public <T extends View> T findById(Object source, int id)
			{
				return (T)((Activity)source).findViewById(id);
			}

			@SuppressWarnings("unchecked") @Override public <T extends View> List<T> findByInstance(Object source, Class instanceType)
			{
				return (List<T>)findAllChildrenByClass((ViewGroup)((Activity)source).findViewById(android.R.id.content), instanceType);
			}

			@Override public Object findFragmentById(Object source, int id)
			{
				Object fragment = null;

				if (source instanceof FragmentActivity && fragment == null)
				{
					fragment = ((FragmentActivity)source).getSupportFragmentManager().findFragmentById(id);
				}
				else if (source instanceof Activity && VERSION.SDK_INT >= 11)
				{
					fragment = ((Activity)source).getFragmentManager().findFragmentById(id);
				}

				return fragment;
			}

			@Override public Object findFragmentByTag(Object source, String tag)
			{
				Object fragment = null;

				if (source instanceof Activity && VERSION.SDK_INT >= 11)
				{
					fragment = ((Activity)source).getFragmentManager().findFragmentByTag(tag);
				}
				else if (source instanceof FragmentActivity)
				{
					fragment = ((FragmentActivity)source).getSupportFragmentManager().findFragmentByTag(tag);
				}

				return fragment;
			}
		};

		public abstract <T extends View> T findById(Object source, int id);
		public abstract <T extends View> List<T> findByInstance(Object source, Class instanceType);
		public abstract Object findFragmentById(Object source, int id);
		public abstract Object findFragmentByTag(Object source, String tag);
	}

	private Views()
	{
		// Empty
	}

	/**
	 * Gets all views of a parent that match an class (recursive)
	 * @param parent The parent view
	 * @param instance The class to check
	 * @return An array of views
	 */
	public static <T extends View> List<T> findAllChildrenByClass(ViewGroup parent, Class<T> instance)
	{
		List<View> views = new ArrayList<View>();
		int childCount = parent.getChildCount();

		for (int childIndex = 0; childIndex < childCount; childIndex++)
		{
			View child = parent.getChildAt(childIndex);

			if (child != null && child.getClass() == instance)
			{
				views.add(child);
			}

			if (child instanceof ViewGroup)
			{
				views.addAll(findAllChildrenByClass((ViewGroup)child, instance));
			}
		}

		return (List<T>)views;
	}

	/**
	 * Gets all views of a parent that match an class (recursive)
	 * @param parent The parent view
	 * @param instance The class to check by instance
	 * @return An array of views
	 */
	public static <T extends View> List<T> findAllChildrenByInstance(ViewGroup parent, Class<T> instance)
	{
		List<View> views = new ArrayList<View>();
		int childCount = parent.getChildCount();

		for (int childIndex = 0; childIndex < childCount; childIndex++)
		{
			View child = parent.getChildAt(childIndex);

			if (child != null && instance.isAssignableFrom(child.getClass()))
			{
				views.add(child);
			}

			if (child instanceof ViewGroup)
			{
				views.addAll(findAllChildrenByInstance((ViewGroup)child, instance));
			}
		}

		return (List<T>)views;
	}

	/**
	 * Resets all @InjectView annotated members to null.
	 * Use this in the `onDestroyView()` of your fragment
	 * @param target The target class
	 */
	public static void reset(Object target)
	{
		ArrayList<Field> fields = new ArrayList<Field>();
		Class objOrSuper = target.getClass();

		if (!objOrSuper.isAnnotationPresent(Injectable.class))
		{
			Log.e("InjectView", "No Injectable annotation for class " + objOrSuper);
			return;
		}

		while (objOrSuper.isAnnotationPresent(Injectable.class))
		{
			for (Field field : objOrSuper.getDeclaredFields())
			{
				if (field.isAnnotationPresent(InjectView.class) || field.isAnnotationPresent(InjectFragment.class))
				{
					fields.add(field);
				}
			}

			objOrSuper = objOrSuper.getSuperclass();
		}

		for (Field field : fields)
		{
			Annotation annotation = field.getAnnotation(InjectView.class);

			if (annotation == null)
			{
				annotation = field.getAnnotation(InjectFragment.class);
			}

			if (annotation != null)
			{
				try
				{
					field.setAccessible(true);
					field.set(target, null);
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Finds a view from an id in a view
	 * @param id The id of the view to find
	 * @param v The view in which to find
	 * @return The view or null
	 */
	public static <T extends View> T findViewById(int id, View v)
	{
		return Finder.VIEW.findById(v, id);
	}

	/**
	 * Finds a view from an id in an activity
	 * @param id The id of the view to find
	 * @param a The activity to search in
	 * @return The view or null
	 */
	public static <T extends View> T findViewById(int id, Activity a)
	{
		return Finder.ACTIVITY.findById(a, id);
	}

	/**
	 * Injects all @InjectView and @OnClick members and methods in an activity
	 * @param target The activity to inject and find the views
	 */
	public static void inject(Activity target)
	{
		inject(target, target, Finder.ACTIVITY);
	}

	/**
	 * Injects all @InjectView and @OnClick members and methods in target from
	 * {@param source}
	 * @param target The class to inject
	 * @param source The activity to find the views
	 */
	public static void inject(Object target, Activity source)
	{
		inject(target, source, Finder.ACTIVITY);
	}

	/**
	 * Injects all @InjectView and @OnClick members and methods in target from
	 * {@param source}
	 * @param target The class to inject
	 * @param source The view to find the views
	 */
	public static void inject(Object target, View source)
	{
		inject(target, source, Finder.VIEW);
	}

	private static void inject(final Object target, Object source, Finder finder)
	{
		if (target.getClass().getDeclaredFields() != null)
		{
			ArrayList<Method> methods = new ArrayList<Method>();
			ArrayList<Field> fields = new ArrayList<Field>();
			Class objOrSuper = target.getClass();

			if (!objOrSuper.isAnnotationPresent(Injectable.class))
			{
				Log.e("InjectView", "No Injectable annotation for class " + objOrSuper);
				return;
			}

			while (objOrSuper.isAnnotationPresent(Injectable.class))
			{
				for (Field field : objOrSuper.getDeclaredFields())
				{
					if (field.isAnnotationPresent(InjectView.class)
					|| field.isAnnotationPresent(InjectViews.class)
					|| field.isAnnotationPresent(InjectFragment.class)
					|| field.isAnnotationPresent(OnClick.class))
					{
						fields.add(field);
					}
				}

				for (Method method : objOrSuper.getDeclaredMethods())
				{
					if (method.isAnnotationPresent(OnClick.class))
					{
						methods.add(method);
					}
				}

				objOrSuper = objOrSuper.getSuperclass();
			}

			for (Field field : fields)
			{
				if (field.isAnnotationPresent(InjectView.class))
				{
					InjectView a = (InjectView)field.getAnnotation(InjectView.class);

					try
					{
						field.setAccessible(true);

						int id = ((InjectView)a).value();
						if (id < 1)
						{
							String key = ((InjectView)a).id();
							if (TextUtils.isEmpty(key))
							{
								key = field.getName();
								key = key.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase(Locale.ENGLISH);
							}

							Field idField = R.id.class.getField(key);
							id = idField.getInt(null);
						}

						View v = finder.findById(source, id);

						if (v != null)
						{
							field.set(target, v);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else if (field.isAnnotationPresent(InjectViews.class))
				{
					try
					{
						InjectViews annotation = (InjectViews)field.getAnnotation(InjectViews.class);
						field.setAccessible(true);

						int[] ids = annotation.value();
						String[] strIds = annotation.id();
						Class[] instances = annotation.instances();

						List<View> views = new ArrayList<View>(ids.length);

						if (ids.length > 0)
						{
							for (int index = 0; index < ids.length; index++)
							{
								View v = finder.findById(source, ids[index]);
								views.add(index, v);
							}
						}
						else if (strIds.length > 0)
						{
							for (int index = 0; index < ids.length; index++)
							{
								String key = annotation.id()[index];
								Field idField = R.id.class.getField(key);
								int id = idField.getInt(null);

								View v = finder.findById(source, id);
								views.add(index, v);
							}
						}
						else if (instances.length > 0)
						{
							for (int index = 0; index < instances.length; index++)
							{
								List<View> v = finder.findByInstance(source, instances[index]);
								views.addAll(v);
							}
						}

						field.set(target, views);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else if (field.isAnnotationPresent(InjectFragment.class))
				{
					InjectFragment annotation = (InjectFragment)field.getAnnotation(InjectFragment.class);

					try
					{
						field.setAccessible(true);

						int id = ((InjectFragment)annotation).value();
						Object fragment = null;

						if (id < 1)
						{
							String tag = ((InjectFragment)annotation).tag();

							fragment = finder.findFragmentByTag(source, tag);
						}
						else
						{
							fragment = finder.findFragmentById(source, id);
						}

						if (fragment != null)
						{
							field.set(target, fragment);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				if (field.isAnnotationPresent(OnClick.class))
				{
					OnClick annotation = (OnClick)field.getAnnotation(OnClick.class);

					try
					{
						if (field.get(target) != null)
						{
							final View view = ((View)field.get(target));

							if (!TextUtils.isEmpty(annotation.method()))
							{
								final String clickName = annotation.method();
								view.setOnClickListener(new View.OnClickListener()
								{
									@Override public void onClick(View v)
									{
										try
										{
											Class<?> c = Class.forName(target.getClass().getCanonicalName());
											Method m = c.getMethod(clickName, View.class);
											m.invoke(target, v);
										}
										catch (Exception e)
										{
											throw new IllegalArgumentException("Method not found " + clickName);
										}
									}
								});
							}
							else
							{
								view.setOnClickListener((View.OnClickListener)target);
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}

			for (final Method method : methods)
			{
				if (method.isAnnotationPresent(OnClick.class))
				{
					final OnClick annotation = (OnClick)method.getAnnotation(OnClick.class);
					final String clickName = method.getName();

					try
					{
						int id = annotation.value();
						if (id < 1)
						{
							String key = annotation.id();

							if (TextUtils.isEmpty(key))
							{
								key = clickName;
								key = key.replaceAll("^(on)?(.*)Click$", "$2");
								key = key.replaceAll("(.)([A-Z])", "$1_$2").toLowerCase(Locale.ENGLISH);
							}

							Field field = R.id.class.getField(key);
							id = field.getInt(null);
						}

						View view = finder.findById(source, id);
						view.setOnClickListener(new View.OnClickListener()
						{
							@Override public void onClick(View v)
							{
								try
								{
									if (method != null && method.getParameterTypes().length > 0)
									{
										Class<?> paramType = method.getParameterTypes()[0];
										method.setAccessible(true);
										method.invoke(target, paramType.cast(v));
									}
									else if (method != null && method.getParameterTypes().length < 1)
									{
										method.setAccessible(true);
										method.invoke(target);
									}
									else
									{
										new IllegalArgumentException("Failed to find method " + clickName + " with nil or View params").printStackTrace();
									}
								}
								catch (InvocationTargetException e)
								{
									e.printStackTrace();
								}
								catch (IllegalAccessException e)
								{
									e.printStackTrace();
								}
							}
						});
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Base property applier class
	 * @param <T> The view type
	 * @param <V> The value type
	 */
	public static abstract class Property<T extends View, V>
	{
		public abstract void apply(T view, V value);
	}

	/**
	 * The standard view property applier class
	 */
	public static class ViewProperty
	{
		/**
		 * Visibility applier
		 */
		public static final Property<? extends View, Integer> VISIBILITY = new Property<View, Integer>()
		{
			@Override public void apply(View view, Integer value)
			{
				view.setVisibility(value);
			}
		};

		/**
		 * Enabled applier
		 */
		public static final Property<? extends View, Boolean> ENABLED = new Property<View, Boolean>()
		{
			@Override public void apply(View view, Boolean value)
			{
				view.setEnabled(value);
			}
		};

		/**
		 * Animation applier
		 */
		public static final Property<? extends View, ? extends Animation> SET_ANIMATION = new Property<View, Animation>()
		{
			@Override public void apply(View view, Animation value)
			{
				view.setAnimation(value);
			}
		};

		/**
		 * Animation start applier
		 */
		public static final Property<? extends View, Void> START_ANIMATION = new Property<View, Void>()
		{
			@Override public void apply(View view, Void value)
			{
				if (view.getAnimation() != null)
				{
					view.getAnimation().start();
				}
			}
		};

		/**
		 * Animation applier
		 */
		public static final Property<? extends View, ? extends Animation> SET_START_ANIMATION = new Property<View, Animation>()
		{
			@Override public void apply(View view, Animation value)
			{
				view.setAnimation(value);
			}
		};
	}

	/**
	 * The standard view property applier class
	 */
	public static class TextViewProperty
	{
		/**
		 * Text applier
		 */
		public static final Property<? extends TextView, String> TEXT = new Property<TextView, String>()
		{
			@Override public void apply(TextView view, String value)
			{
				view.setText(value);
			}
		};
	}

	/**
	 * Applies a value to the list of views by using the {@link Property} class
	 * @param views The list of views to apply the value to
	 * @param property The property applier class
	 * @param value The value to apply to the views
	 * @return The instance of Views
	 */
	public static <T extends View, V> Views apply(List<T> views, Property<T, V> property, V value)
	{
		if (views != null)
		{
			for (T view : views)
			{
				property.apply(view, value);
			}
		}

		return instance;
	}

	/**
	 * Applies a value to the list of views by using the {@link Property} class
	 * @param views The views to apply the value to
	 * @param property The property applier class
	 * @param value The value to apply to the views
	 * @return The instance of Views
	 */
	public static <T extends View, V> Views apply(T[] views, Property<T, V> property, V value)
	{
		if (views != null)
		{
			for (T view : views)
			{
				property.apply(view, value);
			}
		}

		return instance;
	}
}
