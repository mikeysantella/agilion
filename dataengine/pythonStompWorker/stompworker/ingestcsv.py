
import odo
# from odo import discover
import datashape as ds
import sqlalchemy as sa

#import logging
#logging.basicConfig()
#logging.getLogger('sqlalchemy.engine').setLevel(logging.INFO)


class IngestCsv:

    def __init__(self, dbScheme, sqlConnect, dbName):
        self.sqlConnect = sqlConnect
        self.dbName = dbName
        self.dbscheme = dbScheme
        IngestCsv.createDatabaseIfNotExists(dbScheme, sqlConnect, dbName)
        # TODO: close connection when done
        self.db = sa.create_engine(self.dbscheme + '://' + sqlConnect + '/' + dbName)

    @staticmethod
    def createDatabaseIfNotExists(dbscheme, sqlConnect, dbName):
        db = sa.create_engine(dbscheme + '://' + sqlConnect + '/')
        conn = db.connect()
        # https://stackoverflow.com/questions/6506578/how-to-create-a-new-database-using-sqlalchemy
        conn.execute("COMMIT")
        conn.execute('CREATE DATABASE IF NOT EXISTS {}'.format(dbName))
        conn.close()
        
    def ingestCsvToTable(self, csvSourcedata, csvEncoding, tableName, tableDShapeStr, hasHeader):
        sqlResourceStr = self.dbscheme + '://' + self.sqlConnect + '/' + self.dbName + '::' + tableName
        tableDshape = ds.dshape(tableDShapeStr)
        # dshape1 = odo.discover(odo.resource('INTEL_datasets/TIDE_sample_data.csv',  encoding='latin1'))
        # print(dshape1)
        
        t = odo.odo(csvSourcedata, sqlResourceStr,
            encoding=csvEncoding, dshape=tableDshape, has_header=hasHeader)
        print('Ingested {} to {}'.format(csvSourcedata, sqlResourceStr))
        
        if False:
            dataDir = '/home/dlam/dev/agilionReal/dataengine/dataio/'
            exportDir = dataDir + 'mysql/'
            import os
            if not os.path.exists(exportDir):
                os.makedirs(exportDir)
                os.chmod(exportDir, 0o777) 
            import time
            odo.odo(sqlResourceStr, exportDir + '/export' + str(int(time.time())) + '.csv')

