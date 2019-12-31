package org.mybatis.spring.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.Import;

/**
 * Use this annotation to register MyBatis mapper interfaces when using Java Config. It performs when same work as
 * {@link MapperScannerConfigurer} via {@link MapperScannerRegistrar}.
 *
 * <p>
 * Configuration example:
 * </p>
 * 
 * <pre class="code">
 * &#064;Configuration
 * &#064;MapperScan("org.mybatis.spring.sample.mapper")
 * public class AppConfig {
 *
 *   &#064;Bean
 *   public DataSource dataSource() {
 *     return new EmbeddedDatabaseBuilder().addScript("schema.sql").build();
 *   }
 *
 *   &#064;Bean
 *   public DataSourceTransactionManager transactionManager() {
 *     return new DataSourceTransactionManager(dataSource());
 *   }
 *
 *   &#064;Bean
 *   public SqlSessionFactory sqlSessionFactory() throws Exception {
 *     SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
 *     sessionFactory.setDataSource(dataSource());
 *     return sessionFactory.getObject();
 *   }
 * }
 * </pre>
 *
 * @author Michael Lanyon
 * @author Eduardo Macarron
 *
 * @since 1.2.0
 * @see MapperScannerRegistrar
 * @see MapperFactoryBean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
//通过spring @import注解方式将bean注入到IOC容器中
@Import(MapperScannerRegistrar.class)
@Repeatable(MapperScans.class)
public @interface MapperScan {

	/**
	 * 扫描指定包中的mapper接口,不会扫描类 
	 * {@code @MapperScan("com.demo.mapper")}：
	 * 		扫描指定包中的接口
	 * {@code @MapperScan("com.demo.*.mapper")}：
	 * 		一个*代表任意字符串，但只代表一级包,比如可以扫到com.demo.aaa.mapper,不能扫到com.demo.aaa.bbb.mapper 
	 * {@code @MapperScan("com.demo.**.mapper")}：
	 * 		两个*代表任意个包,比如可以扫到com.demo.aaa.mapper,也可以扫到com.demo.aaa.bbb.mapper
	 * 
	 * {@code @MapperScan("org.my.pkg")} 等效于  {@code @MapperScan(basePackages = "org.my.pkg"})}
	 * 
	 * @return base package names
	 */
	String[] value() default {};

	/**
	 *扫描指定包中的mapper接口,不会扫描类 
	 * @return base package names for scanning mapper interface
	 */
	String[] basePackages() default {};

	/**
	 * 使用场景：
	 * 	通常在多数据源的情况下使用。
	 * @return the bean name of {@code SqlSessionTemplate}
	 */
	String sqlSessionTemplateRef() default "";

	/**
	 * 指定SqlSessionFactoryBean对象名
	 * 使用场景：通常在多数据源的情况下使用。
	 * @return the bean name of {@code SqlSessionFactory}
	 */
	String sqlSessionFactoryRef() default "";

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
	 * package of each class specified will be scanned.
	 * <p>
	 * Consider creating a special no-op marker class or interface in each package that serves no purpose other than being
	 * referenced by this attribute.
	 *
	 * @return classes that indicate base package for scanning mapper interface
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * The {@link BeanNameGenerator} class to be used for naming detected components within the Spring container.
	 *
	 * @return the class of {@link BeanNameGenerator}
	 */
	Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

	/**
	 * This property specifies the annotation that the scanner will search for.
	 * <p>
	 * The scanner will register all interfaces in the base package that also have the specified annotation.
	 * <p>
	 * Note this can be combined with markerInterface.
	 *
	 * @return the annotation that the scanner will search for
	 */
	Class<? extends Annotation> annotationClass() default Annotation.class;

	/**
	 * This property specifies the parent that the scanner will search for.
	 * <p>
	 * The scanner will register all interfaces in the base package that also have the specified interface class as a
	 * parent.
	 * <p>
	 * Note this can be combined with annotationClass.
	 *
	 * @return the parent that the scanner will search for
	 */
	Class<?> markerInterface() default Class.class;

	/**
	 * Specifies a custom MapperFactoryBean to return a mybatis proxy as spring bean.
	 *
	 * @return the class of {@code MapperFactoryBean}
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends MapperFactoryBean> factoryBean() default MapperFactoryBean.class;

	/**
	 * Whether enable lazy initialization of mapper bean.
	 *
	 * <p>
	 * Default is {@code false}.
	 * </p>
	 * 
	 * @return set {@code true} to enable lazy initialization
	 * @since 2.0.2
	 */
	String lazyInitialization() default "";

}
