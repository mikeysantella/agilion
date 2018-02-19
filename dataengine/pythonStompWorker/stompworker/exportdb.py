
import sqlalchemy as sa

#import logging
#logging.basicConfig()
#logging.getLogger('sqlalchemy.engine').setLevel(logging.INFO)


class ExportDb:

    def __init__(self, dbScheme, sqlConnect, dbName):
        self.sqlConnect = sqlConnect
        self.dbName = dbName
        self.dbscheme = dbScheme
        # TODO: close connection when done
        self.db = sa.create_engine(self.dbscheme + '://' + sqlConnect + '/' + dbName)

    def getTable(self, tableName):
        md = sa.MetaData()
        md.reflect(bind=self.db)
        # print(md.tables)
        my_table = md.tables[tableName]
        return my_table

    def exportToNeoCsv(self, headerStr, selectExpr, exportedCsv):

    #    import re
    #    sqlite_date_type=sa.dialects.sqlite.DATE(
    #        storage_format="%(month)02d/%(day)02d/%(year)02d", 
    #        regexp=re.compile("(?P<month>\d+)/(?P<day>\d+)/(?P<year>\d+)")
    #        )
        # my_table.c['Date of Birth'].type=sqlite_date_type
        # print(my_table.c)
        
        # create exportDir and allow mysql user in Docker to write to directory
        import os, stat
        exportDir=os.path.dirname(exportedCsv)
        if not os.path.exists(exportDir):
            os.makedirs(exportDir)
        os.chmod(exportDir, stat.S_IRWXU | stat.S_IRWXG | stat.S_IRWXO)
        
        e = SelectIntoOutfile(headerStr, selectExpr, exportedCsv)
        self.db.execute(e)


from sqlalchemy.sql.expression import Executable, ClauseElement
from sqlalchemy.ext import compiler


class SelectIntoOutfile(Executable, ClauseElement):

    def __init__(self, headerStr, select, file):
        self.headerStr = headerStr
        self.select = select
        self.file = file


@compiler.compiles(SelectIntoOutfile)
def compile(element, compiler, **kw):
    colNames = []
    for col in element.select.columns:
        colNames.append("'{}'".format(col.name))
    #selectStr = "SELECT " + ','.join(colNames)
    selectStr = "SELECT "+element.headerStr
    return selectStr + " UNION ALL " + "{} INTO OUTFILE '{}' FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n'".format(
        compiler.process(element.select), element.file
    )
