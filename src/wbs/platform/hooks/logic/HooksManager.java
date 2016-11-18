package wbs.platform.hooks.logic;

import static wbs.utils.etc.TypeUtils.classNameSimple;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Log4j
@SingletonComponent ("hooksManager")
public
class HooksManager {

	// singleton dependencies

	@SingletonDependency
	List <HooksProxy> proxies =
		Collections.emptyList ();

	@SingletonDependency
	List <HooksTarget> targets =
		Collections.emptyList ();

	// life cycle

	@NormalLifecycleSetup
	public
	void afterPropertiesSet ()
		throws Exception {

		log.debug (
			"Initialising");

		for (HooksProxy proxy
				: proxies) {

			initProxy (
				proxy);

		}

		log.debug (
			"Ready");

	}

	public
	void initProxy (
			HooksProxy proxy) {

		log.debug (
			stringFormat (
				"Initialising proxy type %s",
				classNameSimple (
					proxy.getClass ())));

		Object delegate =
			createDelegate (
				proxy.getParentClass (),
				ImmutableList.copyOf (
					Iterables.filter (
						targets,
						proxy.getTargetClass ())));

		proxy.setDelegate (
			delegate);

	}

	public
	Object createDelegate (
			Class<?> parentClass,
			final Collection<?> targets) {

		Class<?> proxyClass =
			Proxy.getProxyClass (
				parentClass.getClassLoader (),
				new Class [] { parentClass });

		InvocationHandler invocationHandler =
			new InvocationHandler () {

			@Override
			public
			Object invoke (
					Object target,
					Method method,
					Object[] args)
				throws Throwable {

				if (targets != null) {
					for (Object hook : targets) {
						method.invoke (hook, args);
					}
				}

				return null;

			}

		};

		Constructor<?> constructor;

		try {

			constructor =

			proxyClass.getConstructor (
				new Class [] {
					InvocationHandler.class
				});

		} catch (NoSuchMethodException exception) {

			throw new RuntimeException (
				exception);

		}

		Object proxyObject;
		try {

			proxyObject =
				constructor.newInstance (
					invocationHandler);

		} catch (InvocationTargetException exception) {

			throw new RuntimeException (
				exception);

		} catch (InstantiationException exception) {

			throw new RuntimeException (
				exception);

		} catch (IllegalAccessException exception) {

			throw new RuntimeException (
				exception);

		}

		return parentClass.cast (
			proxyObject);

	}

}
