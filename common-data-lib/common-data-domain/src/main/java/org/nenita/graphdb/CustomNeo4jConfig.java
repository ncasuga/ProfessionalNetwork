package org.nenita.graphdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.nenita.domain.UUIDable;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.event.BeforeSaveEvent;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.google.common.reflect.ClassPath;

/**
 * Neo4j custom configuration that does the following:
 * 
 * <ul>
 * <li>Defines the base packaging for scanning repositories and domain entities</li>
 * <li>Provides an annotation to determine the actual location of domain entities for each component</li>
 * <li>Provides a interceptor that would generate UUID every time a {@link org.nenita.domain.UUiDable} entity is persisted
 * </li>
 * </ul>
 * 
 * @author nenita
 *
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "org.nenita.*.repository")
@EnableTransactionManagement
@ComponentScan(basePackages = { "org.nenita.*.domain" })
public class CustomNeo4jConfig extends Neo4jConfiguration {

	private List<String> packages = new ArrayList<String>();
	private Set<String> uuidables = new HashSet<String>();

	@Bean
	public SessionFactory getSessionFactory() {
		if (packages.isEmpty()) {
			throw new RuntimeException("Packages containing neo4j domain entities should be specified");
		}
		String[] arr = new String[packages.size()];
		return new SessionFactory(packages.toArray(arr));
	}

	@Bean
	public Session getSession() throws Exception {
		return super.getSession();
	}

	/*
	 * This is called only once 
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostConstruct
	public void prepareSession() throws Exception {
		ClassPath classpath = ClassPath.from(Thread.currentThread().getContextClassLoader());
		for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive("org.nenita")) {
			Class clazz = classInfo.load();
			if (clazz.isAnnotationPresent(CustomNeo4jDomainPackage.class)) {
				CustomNeo4jDomainPackage domainPackage = (CustomNeo4jDomainPackage) clazz
						.getAnnotation(CustomNeo4jDomainPackage.class);
				packages.addAll(Arrays.asList(domainPackage.packages()));

			} else if (UUIDable.class.isAssignableFrom(clazz)) {
				// Determine which ones have UUID fields
				uuidables.add(clazz.getName());
			}
		}
	}

	@Bean
	ApplicationListener<BeforeSaveEvent> beforeSaveEventApplicationListener() {
		return new ApplicationListener<BeforeSaveEvent>() {
			@Override
			public void onApplicationEvent(BeforeSaveEvent event) {
				if (uuidables.contains(event.getEntity().getClass().getName())) {
					UUIDable entity = (UUIDable) event.getEntity();
					// Generate only entity uuid if not present yet!
					if (entity.getUuid() == null || entity.getUuid().trim().length() == 0) {
						entity.setUuid(UUID.randomUUID().toString());
					}
				}
			}
		};
	}

	// Visible for testing
	boolean isClassUuidable(String className) {
		return uuidables.contains(className);
	}

	// Visible for testing
	List<String> getPackages() {
		return packages;
	}
}
