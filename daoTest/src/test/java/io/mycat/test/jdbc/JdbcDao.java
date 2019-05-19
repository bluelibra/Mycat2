package io.mycat.test.jdbc;

import io.mycat.MycatCore;
import io.mycat.proxy.AsyncTaskCallBack;
import io.mycat.proxy.ProxyRuntime;
import io.mycat.proxy.session.Session;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;

/**
 * @author jamie12221
 * @date 2019-05-19 18:08
 **/
public abstract class JdbcDao {

  final static String url = "jdbc:mysql://localhost:8066/test";
  final static String username = "root";
  final static String password = "123456";

  static {
    // 加载可能的驱动
    List<String> drivers = Arrays.asList(
        "com.mysql.jdbc.Driver");

    for (String driver : drivers) {
      try {
        Class.forName(driver);
      } catch (ClassNotFoundException ignored) {
      }
    }
  }

  public static void loadModule(String module, AsyncTaskCallBack task)
      throws IOException, ExecutionException, InterruptedException {
    String resourcesPath = ProxyRuntime.getResourcesPath();
    Path resolve = Paths.get(resourcesPath).resolve("io/mycat/test/jdbc").resolve(module);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    final CompletableFuture<String> future = new CompletableFuture<>();
    MycatCore.startup(resolve.toAbsolutePath().toString(), new AsyncTaskCallBack() {
      @Override
      public void finished(Session session, Object sender, boolean success, Object result,
          Object attr) {
        if (success) {
          executor.submit(() -> {
            task.finished(null, null, success, result, future);
          });
        } else {
          Assert.fail(result.toString());
        }
      }
    });
    future.get();
  }

  public static void compelete(Object fulture) {
    CompletableFuture completableFuture = (CompletableFuture) fulture;
    completableFuture.complete(null);
  }

  public static Connection getConnection() throws SQLException {
    Connection connection = null;
    connection = DriverManager
                     .getConnection(getUrl(), getUsername(),
                         getPassword());
    return connection;
  }

  public static String getUrl() {
    return url;
  }

  public static String getUsername() {
    return username;
  }

  public static String getPassword() {
    return password;
  }
}
