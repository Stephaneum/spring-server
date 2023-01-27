import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.12"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"

	val kotlinVersion = "1.6.21"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
	kotlin("plugin.jpa") version kotlinVersion
}

group = "de.stephaneum"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	// General
	implementation("org.springframework.boot:spring-boot-starter-freemarker")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")

	//OAuth2
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("com.azure.spring:spring-cloud-azure-starter-active-directory:4.4.0")

	// DB
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")
	runtimeOnly("mysql:mysql-connector-java")

	// Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Specific Libraries
	implementation("org.apache.pdfbox:pdfbox:2.0.25")
	implementation("commons-io:commons-io:2.11.0")
	implementation("org.jsoup:jsoup:1.14.3")
	implementation("com.drewnoakes:metadata-extractor:2.17.0")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.2")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

task<Copy>("updateVue") {
	from("src/main/vue/dist/static", "src/main/vue/dist/index.html")
	into("$buildDir/resources/main/static")
}

tasks.withType<ProcessResources> {
	dependsOn("updateVue")
}