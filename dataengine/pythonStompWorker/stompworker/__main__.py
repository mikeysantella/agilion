#!/usr/bin/env python

from sqlalchemy.sql import text

class MsgListener:

    def meth1(self, msg, s):
        print("MsgListener got: msg=", msg, " s=",s)
#         if s.command == 'QUERY':
#             self.query(msg, s)
        if s.command == 'INGEST':
            self.ingest(msg, s)
        elif s.command == 'EXPORT_TO_NEOCSV':
            self.exportToNeoCsv(msg, s)
#         elif s.command == 'SELECT':
#             self.select(msg, s)
        elif s.command == 'END':
            with st.listenerDoneLock:
                st.listenerDoneLock.notifyAll()
        else:
            raise ValueError('Unknown command='+s.command)

    def ingest(self, msg, s):
        from stompworker.ingestcsv import IngestCsv
#         config = {
#             'sqlConnect':  s.sqlConnect,  # 'root:dataengine-mysql@localhost:3306', 
#             'dbName': s.dbName,  # 'tempdb'
#             'tableName': s.tableName,  # ='tide'
#             # 'dataDir': s.dataDir,  #='/home/dlam/dev/agilionReal/dataengine/dataio/'
#             #'exportDir': s.exportDir,  # =dataDir+'mysql/'
#             'sourcedataCsv': s.sourcedataCsv,  # =dataDir+'INTEL_datasets/TIDE_sample_data.csv'
#             'dshape': s.dshape
#         }
        s.hasHeader=bool(s.hasHeader)
        ingester = IngestCsv('mysql+pymysql', s.sqlConnect, s.dbName)
        ingester.ingestCsvToTable(s.sourcedataCsv, 'latin1', s.tableName, s.dshape, s.hasHeader)

    def exportToNeoCsv(self, msg, s):
        from stompworker.exportdb import ExportDb
        exporter = ExportDb('mysql+pymysql', s.sqlConnect, s.dbName)
        
        #s.selectFields='concat(lastName, " ", firstName) as personId, citizenship as countryId, citizenship, birthCountry'
        colArray=[text(s.selectFields)]
        my_table = exporter.getTable(s.tableName)
        selectExpr2 = sa.select(colArray).select_from(my_table)
        if(bool(s.selectDistinct)):
           selectExpr2=selectExpr2.distinct()
        #s.exportCsvPath = exportDir + 'countriesDyn.csv'
        exporter.exportToNeoCsv(s.selectHeader, selectExpr2, s.exportCsvPath)
        
#from stompworker.settingsreader import GenSql
#    def query(self, msg, s):
#        config = {
#          'domainfieldsFile': s.domainfieldsFile,
#          'fieldmapFile': s.fieldmapFile,
#          'dbname': s.dbname,
#          'tablename': s.tablename,
#          'csvFile': s.csvFile
#        }
#        GenSql.genPopulateSql(**config)
#
#    def select(self, msg, s):
#        # use https://www.sqlalchemy.org/ instead?
#        import MySQLdb
#        config = {
#          'user': s.user,  # 'root',
#          'password': s.pw,  # 'my-secret-pw',
#          'host': s.host,  # '127.0.0.1',
#          'database': s.db  # 'thegeekstuff'
#        }
#        try:
#            cnx = MySQLdb.connect(**config)
#            cursor = cnx.cursor()
#
#            query = (GenSql.genSelectAll(s.tablename))
#            print("select=", query)
#            cursor.execute(query)
#            # https://stackoverflow.com/questions/9942594/unicodeencodeerror-ascii-codec-cant-encode-character-u-xa0-in-position-20/9942822
#            # for r in cursor:
#              # print("row: {}".format(str(r).encode('utf-8')))
#        finally:
#            cursor.close()
#            cnx.close()
#
#            
#def meth1(msg, s):
#    GenSql.genPopulateSql(**s)
  
import sqlalchemy as sa


def exportToNeo(exporter, tableName, exportDir):
    my_table = exporter.getTable(tableName)
    selectExpr1 = sa.select([
        (my_table.c.lastName + my_table.c.firstName).label('personId'),
        my_table.c.lastName,
        my_table.c.firstName,
        my_table.c.nickname,
        my_table.c.terroristName,
        my_table.c.biometricID,
        my_table.c.maritalStatus,
        my_table.c.gender,
        my_table.c.citizenship.label('countryId'),
        my_table.c.dob,
        my_table.c.birthCountry,
        my_table.c.isNatCitizen,
        my_table.c.hasFlightTraining,
        my_table.c.isTsaNoFly,
        my_table.c.visaNumber,
        my_table.c.visaRevocation,
        ])
    exportedCsv = exportDir + 'persons.csv'
    exporter.exportToNeoCsv(selectExpr1, exportedCsv)
    
    selectExpr2 = sa.select([
        (my_table.c.lastName + my_table.c.firstName).label('personId'),
        my_table.c.citizenship.label('countryId'),
        my_table.c.citizenship,
        my_table.c.birthCountry,
        ]).distinct()
    exportedCsv = exportDir + 'countries.csv'
    exporter.exportToNeoCsv(selectExpr2, exportedCsv)
    
    from sqlalchemy.sql import text, column
    colArray=[
        (column('lastName') + column('firstName')).label('personId'),
        column('citizenship').label('countryId'),
        column('citizenship'),
        column('birthCountry'),
        ]
    print("colArray1=", colArray)
    
    colArray=[
        text('concat(lastName, " ", firstName) as personId'),
        column('citizenship').label('countryId'),
        column('citizenship'),
        column('birthCountry'),
        ]
    print("colArray2=", colArray)
    
    colArray=[text('concat(lastName, " ", firstName) as personId, citizenship as countryId, citizenship, birthCountry')]
    print("colArray3=", colArray)
    
    selectExpr2 = sa.select(colArray).select_from(my_table).distinct()
    exportedCsv = exportDir + 'countriesDyn.csv'
    exporter.exportToNeoCsv(selectExpr2, exportedCsv)

    
    if False:
        for row in exporter.db.execute(sa.select([my_table])):
            # print("where: ", str(row).encode('latin-1', 'ignore'))
            # print(row)
            print("*", str(row).encode('utf-8', 'namereplace'))
        
    result = exporter.db.execute(sa.select([my_table])
            .where(my_table.c.gender != None)
            .order_by(my_table.c['birthCountry'])
        )
    if False:
        for row in result:
            print("where: ", str(row).encode('utf-8', 'namereplace'))
            # print(row)
    
# import os
# sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
# print(sys.path)


import sys
import stompworker.stomplistener as sl
if __name__ == "__main__":
    print("argv=", sys.argv)
    if len(sys.argv) == 1:
        sqlConnect = 'root:dataengine-mysql@localhost:3306'
        dataDir = '/home/dlam/dev/agilionReal/dataengine/dataio/'
        sourcedataCsv = dataDir + 'INTEL_datasets/TIDE_sample_data.csv'
        dshapeStr = '''var * {
          'isNatCitizen': ?string,
          'birthCountry': ?string,
          'citizenship': ?string,
          'datasource': ?string,
          'dob': ?string,
          'hasFlightTraining': ?string,
          gender: ?string,
          'firstName': ?string,
          'terroristName': ?string,
          'lastName': ?string,
          nickname: ?string,
          'biometricID': ?string,
          'fbiCaseNumber': ?string,
          'tideIdNumber': int64,
          'lprStatus': ?string,
          'maritalStatus': ?string,
          'tideStatus': ?string,
          'uspStatus': ?string,
          'terroristMembership': ?string,
          'tideCategory': ?string,
          title: ?string,
          'isTsaNoFly': ?string,
          'visaNumber': int64,
          'visaRevocation': ?string
          }'''
        dbName = 'sess123'
        tableName = 'TIDE___1518909015408'
        case=2
        if(case==1):
            from stompworker.ingestcsv import IngestCsv
            ingester = IngestCsv('mysql+pymysql', sqlConnect, dbName)
            ingester.ingestCsvToTable(sourcedataCsv, 'latin1', tableName, dshapeStr, True)
        elif(case==2):
            exportDir = dataDir + 'mysql/sub3/'
            
            # create exportDir and allow mysql user in Docker to write to directory
            import os, stat
            if not os.path.exists(exportDir):
                os.makedirs(exportDir)
            os.chmod(exportDir, stat.S_IRWXU | stat.S_IRWXG | stat.S_IRWXO)
            
            from stompworker.exportdb import ExportDb
            exporter = ExportDb('mysql+pymysql', sqlConnect, dbName)
            exportToNeo(exporter, tableName, exportDir)
    else:
        debugSqlAlchemy=False
        if(debugSqlAlchemy):
            import logging
            logging.basicConfig()
            logging.getLogger('sqlalchemy.engine').setLevel(logging.INFO)

        port = sys.argv[1]
        st = sl.StompListener(hosts=[('localhost', port)])
        inboxQueue = sys.argv[2]
        st.handle(inboxQueue, sl.RunFunctionListener(st, MsgListener().meth1))
        # sl.startStompListener('test', 123,  sl.RunFunctionListener(meth1))
        # sl.startStompListener('test', 123,  sl.CallFunctionListener(locals(),  defMethodName="meth1"))
        # sl.startStompListener('test', 123,  sl.CallMethodListener(MsgListener()))
        
#        import signal
#        signal.signal(signal.SIGINT, st.shutdown)

        with st.listenerDoneLock:
            st.listenerDoneLock.wait()
#        import time
#        time.sleep(10)
        st.shutdown()
        print('Exiting')

