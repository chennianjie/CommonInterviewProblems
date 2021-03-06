package ds.fileparse;

import common.SleepTools;
import ds.common.ProcessBatchQueues;
import ds.common.PropertyUtil;
import ds.common.PropsStr;
import oracle.jdbc.OracleCallableStatement;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
import oracle.sql.JAVA_STRUCT;
import oracle.sql.StructDescriptor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Description:
 * @Author: nianjie.chen
 * @Date: 9/30/2019
 */
public class IncrementalsInsertTask implements Runnable {

    private static int batchNum = Integer.parseInt(PropertyUtil.getPropValue(PropsStr.BatchNumber));
    private String fileName;
    private String uuid;
    private Connection connection;
    private Connection arrayConnection;
    private OracleCallableStatement proc;
    private CountDownLatch endControl;
    public IncrementalsInsertTask() {
    }

    public IncrementalsInsertTask(String fileName, String uuid, Connection connection, Connection arrayConnection, CountDownLatch endControl) {
        this.fileName = fileName;
        this.uuid = uuid;
        this.connection = connection;
        this.arrayConnection =arrayConnection;
    }

    @Override
    public void run() {

        //get data from queue by batch then insert into DB
        boolean done = false;
        List<IncrementalStg> incList = new ArrayList<>();
        try {

            while (!done){
                if (ProcessBatchQueues.IncrementalQueue2.size() == 0) {
                        SleepTools.ms(20000);
                }
                IncrementalStg stg = ProcessBatchQueues.IncrementalQueue2.take();
                if (stg == ParseXML.getDUMMY()) {
                    ProcessBatchQueues.IncrementalQueue2.put(stg);
                    done = true;
                }else {
                    incList.add(stg);
                }
                if (incList.size() == batchNum){
                    callInsertIncProcedure(incList);
                    ProcessBatchQueues.insertNum.addAndGet(100);
                    incList.clear();
                }
            }

            if (!incList.isEmpty()) {
                callInsertIncProcedure(incList);
                ProcessBatchQueues.insertNum.addAndGet(incList.size());
                incList.clear();
            }

        } catch (SQLException e) {
            try {
                done = true;
                ProcessBatchQueues.IncrementalQueue2.put(ParseXML.getDUMMY());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();

        } catch (InterruptedException e) {
            try {
                done = true;
                ProcessBatchQueues.IncrementalQueue2.put(ParseXML.getDUMMY());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            Thread.interrupted();
            e.printStackTrace();
        } finally {


            try {
                if (connection != null) {
                    connection.close();
                }
                if (arrayConnection != null) {
                    arrayConnection.close();
                }
            }catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //endControl change status
            endControl.countDown();
        }

    }

    private  void callInsertIncProcedure(List<IncrementalStg> incList) throws SQLException {
        proc = (OracleCallableStatement) connection
                .prepareCall("{ call RDC_COLLECTED.RDC_INSERT_INC_PDP_PRC(?,?,?,?) }");

        proc.setString(1, fileName);
        proc.setString(2, uuid);
        int bat_index = SDIFileInsertProcessor.batch_index.getAndIncrement();
        proc.setInt(3, bat_index);

        ARRAY resultArr =
              tran2Oracle(arrayConnection, incList,
               bat_index);
        proc.setARRAY(4, resultArr);
        //System.out.println(Thread.currentThread().getName() + "==>batch_index:" +bat_index + "==>incListSize:" + incList.size() + "==>uuid:"+uuid +"==>filename:" +fileName);
        proc.execute();
        proc.close();
    }

    private static ARRAY tran2Oracle(Connection con,
                                     List<IncrementalStg> istgList,
                                     Integer batchIndex) throws SQLException {

        StructDescriptor structDesc = StructDescriptor.createDescriptor(
                "RDC_COLLECTED.RDC_INCR_TYPE", con);
        JAVA_STRUCT[] structs = new JAVA_STRUCT[istgList.size()];
        int listSeq = 0;

        for (int i = 0; i < istgList.size(); i++) {

            Object[] objeArry = new Object[19];
            objeArry[0] = istgList.get(i).getNda_pi();
            objeArry[1] = istgList.get(i).getVersion();
            objeArry[2] = istgList.get(i).getEntity_type();
            objeArry[3] = istgList.get(i).getEntity_sub_type();
            objeArry[4] = istgList.get(i).getEntity_rcs_sub_type();
            objeArry[5] = istgList.get(i).getEntity_event();
            objeArry[6] = istgList.get(i).getProperty_id();
            objeArry[7] = istgList.get(i).getCurrent_value();
            objeArry[8] = istgList.get(i).getClassifier_type();
            objeArry[9] = istgList.get(i).getValid_from();
            objeArry[10] = istgList.get(i).getValid_from_inc_time();
            objeArry[11] = istgList.get(i).getValid_to();
            objeArry[12] = istgList.get(i).getValid_to_inc_time();
            objeArry[13] = istgList.get(i).getLanguage();
            objeArry[14] = istgList.get(i).getBpm_batch_guid();
            objeArry[15] = batchIndex;
            objeArry[16] = istgList.get(i).getCreate_date();
            objeArry[17] = istgList.get(i).getCreate_by();
            objeArry[18] = istgList.get(i).getReference_flag();
            structs[listSeq++] = new JAVA_STRUCT(structDesc, con, objeArry);
        }

        ArrayDescriptor arryDesc = ArrayDescriptor.createDescriptor(
                "RDC_COLLECTED.RDC_INCR_COL_TYPE", con);
        ARRAY list = new ARRAY(arryDesc, con, structs);
//        con.close();
        return list;
    }
}
