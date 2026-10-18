package dev.lokspel.deathswap.config;

import dev.lokspel.deathswap.config.backed.BackedConfig;
import dev.lokspel.deathswap.config.messages.MessagesConfig;
import dev.lokspel.deathswap.config.settings.SettingsConfig;
import java.nio.file.Path;
import java.util.List;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.exception.ConfigException;
import me.whereareiam.configura.feature.comment.CommentFeature;
import me.whereareiam.strata.MigrationStream;
import me.whereareiam.strata.Strata;
import me.whereareiam.strata.adapter.configura.ConfigContext;
import me.whereareiam.strata.adapter.configura.ConfiguraTarget;
import me.whereareiam.strata.adapter.configura.StrataFeature;

/**
 * Loads the configuration files of the plugin and holds what they contain.
 *
 * <p>Each file is bound to its model with Configura: a missing file is created from the defaults
 * the models declare, settings a user's file lacks are added, and what the user wrote is kept.
 * Before that, Strata brings files written by older versions up to date.
 */
public enum DeathSwapConfig {
  ;

  private static final String VERSION_FILE = "config.yml";

  private static Path directory;
  private static Documents documents;
  private static Configura configura;

  /**
   * Upgrades and loads every configuration file of the data folder.
   *
   * @param dataDirectory the plugin's data folder
   * @throws ConfigException if a file cannot be read or does not fit its model
   */
  public static void load(Path dataDirectory) {
    configura = configura();
    migrate(configura, dataDirectory);

    documents = Documents.load(configura, dataDirectory);
    directory = dataDirectory;
  }

  /**
   * Loads every configuration file again. The previous values stay in place if a file cannot be
   * loaded.
   *
   * @throws ConfigException if a file cannot be read or does not fit its model
   */
  public static void reload() {
    documents = Documents.load(configura != null ? configura : (configura = configura()), directory);
  }

  /**
   * Describes a failure to load the configuration with its causes, which name the file and the
   * setting that could not be read.
   *
   * @param failure what {@link #load(Path)} or {@link #reload()} threw
   * @return a single-line description for the console
   */
  public static String describeFailure(Throwable failure) {
    StringBuilder message = new StringBuilder(String.valueOf(failure.getMessage()));
    for (Throwable cause = failure.getCause(); cause != null; cause = cause.getCause()) {
      message.append(" <- ").append(cause.getMessage());
    }
    return message.toString();
  }

  public static SettingsConfig settings() {
    return documents != null ? documents.settings() : new SettingsConfig();
  }

  public static MessagesConfig messages() {
    return documents != null ? documents.messages() : new MessagesConfig();
  }

  public static BackedConfig backed() {
    return documents != null ? documents.backed() : new BackedConfig();
  }

  public static void saveBacked() {
    if (configura != null && directory != null && documents != null) {
      configura.write(directory.resolve("backed.yml"), documents.backed());
    }
  }

  private static Configura configura() {
    return Configura.builder()
        .feature(new StrataFeature())
        .feature(CommentFeature.defaults())
        .build();
  }

  private static void migrate(Configura configura, Path dataDirectory) {
    MigrationStream<ConfigContext> stream =
        MigrationStream.<ConfigContext>builder()
            .id("deathswap/config")
            .target(new ConfiguraTarget(configura, dataDirectory, VERSION_FILE))
            .baseline((files, latest) -> files.exists(VERSION_FILE) ? 0 : latest)
            .build();

    new Strata(List.of(stream)).migrate();
  }

  private record Documents(SettingsConfig settings, MessagesConfig messages, BackedConfig backed) {
    static Documents load(Configura configura, Path directory) {
      return new Documents(
          configura.update(directory.resolve("config.yml"), SettingsConfig.class),
          configura.update(directory.resolve("messages.yml"), MessagesConfig.class),
          configura.update(directory.resolve("backed.yml"), BackedConfig.class));
    }
  }
}
