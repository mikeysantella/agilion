package dataengine.apis;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.deelam.utils.UriUtil;

public class UriCodec {
  public static void main(String[] args) {
    String uriStr=genMySqlUri("mydb", "mytable");
    UriMySql uriSql=parseMySqlUri(uriStr);
    System.out.println(uriSql.getDatabaseName());
  }
  
  public static final String QUERYSTR_TABLE = "table";

  public static String genMySqlUri(String dbname, String tablename) {
    URI uri = UriBuilder.fromPath(dbname) //
        .scheme("mysql").host("local").port(3306) //
        .queryParam(QUERYSTR_TABLE, tablename).build();
    return uri.toString();
  }
  
  public static UriMySql parseMySqlUri(String uriStr) {
    return new UriMySql(URI.create(uriStr));
  }

  @RequiredArgsConstructor
  public static class UriMySql {
    final URI outUri;

    @Getter(lazy=true)
    private final Map<String, String> queryParams = _parseQuery();
    private Map<String, String> _parseQuery() {
      return UriUtil.splitSimpleQuery(outUri.getQuery());
    }
    
    public  String getTablename() {
      return getQueryParams().get(QUERYSTR_TABLE);
    }

    public  String getDatabaseName() {
      String path=outUri.getPath();
      if(path.startsWith("/"))
        return path.substring(1);
      else
        return path;
    }

  }
}
