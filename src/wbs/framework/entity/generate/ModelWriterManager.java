package wbs.framework.entity.generate;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderFactory;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.utils.etc.FormatWriter;

@SingletonComponent ("modelWriterManager")
public
class ModelWriterManager {

	// prototype dependencies

	@Inject
	Provider<BuilderFactory> builderFactoryProvider;

	// collection dependencies

	@Inject
	@ModelWriter
	Map<Class<?>,Provider<Object>> modelWriterProviders;

	// state

	Builder modelWriter;

	// lifecycle

	@PostConstruct
	public
	void setup () {

		BuilderFactory builderFactory =
			builderFactoryProvider.get ();

		for (
			Map.Entry<Class<?>,Provider<Object>> modelWriterEntry
				: modelWriterProviders.entrySet ()
		) {

			builderFactory.addBuilder (
				modelWriterEntry.getKey (),
				modelWriterEntry.getValue ());

		}

		modelWriter =
			builderFactory.create ();

	}

	// implementation

	public
	void write (
			ModelMetaSpec modelMeta,
			List<?> sourceItems,
			FormatWriter javaWriter) {

		modelWriter.descend (
			modelMeta,
			sourceItems,
			javaWriter);

	}

}