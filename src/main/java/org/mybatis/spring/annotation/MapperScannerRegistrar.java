package org.mybatis.spring.annotation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * A {@link ImportBeanDefinitionRegistrar} to allow annotation configuration of MyBatis mapper scanning. Using
 * an @Enable annotation allows beans to be registered via @Component configuration, whereas implementing
 * {@code BeanDefinitionRegistryPostProcessor} will work for XML configuration.
 *
 * @author Michael Lanyon
 * @author Eduardo Macarron
 * @author Putthiphong Boonphong
 *
 * @see MapperFactoryBean
 * @see ClassPathMapperScanner
 * @since 1.2.0
 */
public class MapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(MapperScannerRegistrar.class);

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Since 2.0.2, this method not used never.
	 */
	@Override
	@Deprecated
	public void setResourceLoader(ResourceLoader resourceLoader) {
		// NOP
	}

	/**
	 * importingClassMetadata 是引入 MapperScannerRegistrar类的注解的类的元信息
	 * 例如:
	 * @MapperScan(basePackages = "com.lzh.wechat.demo.dao.wechat")
	 * public class WechatDataSourceConfig
	 * 即 WechatDataSourceConfig类的元信息
	 */
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		// 获取@MapperScan注解里面的属性信息
		AnnotationAttributes mapperScanAttrs = AnnotationAttributes
				.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
		if (mapperScanAttrs != null) {
			// 生成baseBeanName名字 ————>
			// com.lzh.wechat.demo.common.mybatis.WechatDataSourceConfig#MapperScannerRegistrar#0
			registerBeanDefinitions(mapperScanAttrs, registry, generateBaseBeanName(importingClassMetadata, 0));
		}
	}

	@SuppressWarnings("rawtypes")
	void registerBeanDefinitions(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry, String beanName) {

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
		builder.addPropertyValue("processPropertyPlaceHolders", true);

		// interface java.lang.annotation.Annotation
		Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
		if (!Annotation.class.equals(annotationClass)) {
			builder.addPropertyValue("annotationClass", annotationClass);
		}

		// class java.lang.Class
		Class<?> markerInterface = annoAttrs.getClass("markerInterface");
		if (!Class.class.equals(markerInterface)) {
			builder.addPropertyValue("markerInterface", markerInterface);
		}

		// interface org.springframework.beans.factory.support.BeanNameGenerator
		Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
		if (!BeanNameGenerator.class.equals(generatorClass)) {
			builder.addPropertyValue("nameGenerator", BeanUtils.instantiateClass(generatorClass));
		}

		// class org.mybatis.spring.mapper.MapperFactoryBean
		Class<? extends MapperFactoryBean> mapperFactoryBeanClass = annoAttrs.getClass("factoryBean");
		if (!MapperFactoryBean.class.equals(mapperFactoryBeanClass)) {
			builder.addPropertyValue("mapperFactoryBeanClass", mapperFactoryBeanClass);
		}

		String sqlSessionTemplateRef = annoAttrs.getString("sqlSessionTemplateRef");
		if (StringUtils.hasText(sqlSessionTemplateRef)) {
			builder.addPropertyValue("sqlSessionTemplateBeanName", annoAttrs.getString("sqlSessionTemplateRef"));
		}

		// wechatSqlSessionFactory
		String sqlSessionFactoryRef = annoAttrs.getString("sqlSessionFactoryRef");
		if (StringUtils.hasText(sqlSessionFactoryRef)) {
			builder.addPropertyValue("sqlSessionFactoryBeanName", annoAttrs.getString("sqlSessionFactoryRef"));
		}

		// [com.lzh.wechat.demo.dao.wechat]
		List<String> basePackages = new ArrayList<>();
		basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("value")).filter(StringUtils::hasText)
				.collect(Collectors.toList()));

		basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("basePackages")).filter(StringUtils::hasText)
				.collect(Collectors.toList()));

		basePackages.addAll(Arrays.stream(annoAttrs.getClassArray("basePackageClasses")).map(ClassUtils::getPackageName)
				.collect(Collectors.toList()));

		String lazyInitialization = annoAttrs.getString("lazyInitialization");
		if (StringUtils.hasText(lazyInitialization)) {
			builder.addPropertyValue("lazyInitialization", lazyInitialization);
		}

		// 将集合中全类名转换成 逗号 分隔的字符串
		builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));

		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

	}

	private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
		String baseBeanName = importingClassMetadata.getClassName() + "#" + MapperScannerRegistrar.class.getSimpleName()
				+ "#" + index;
		LOGGER.trace(() -> "baseBeanName:" + baseBeanName);
		return baseBeanName;
	}

	/**
	 * A {@link MapperScannerRegistrar} for {@link MapperScans}.
	 * 
	 * @since 2.0.0
	 */
	static class RepeatingRegistrar extends MapperScannerRegistrar {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
				BeanDefinitionRegistry registry) {
			AnnotationAttributes mapperScansAttrs = AnnotationAttributes
					.fromMap(importingClassMetadata.getAnnotationAttributes(MapperScans.class.getName()));
			if (mapperScansAttrs != null) {
				AnnotationAttributes[] annotations = mapperScansAttrs.getAnnotationArray("value");
				for (int i = 0; i < annotations.length; i++) {
					registerBeanDefinitions(annotations[i], registry, generateBaseBeanName(importingClassMetadata, i));
				}
			}
		}
	}

}
