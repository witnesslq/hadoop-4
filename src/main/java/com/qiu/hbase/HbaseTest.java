package com.qiu.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;


public class HbaseTest {
    static Configuration conf=null;
    static {
        conf=HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "DamHadoop1,DamHadoop2,DamHadoop3");//zookeeper服务器地址，要换成自己服务器地址
    }
    //创建表，如果表存在则给出提示
    static void createTable(String tableName,String[] columnFamilys){
        try {
            System.out.println("创建表，如果表存在则给出提示"+"start!"+tableName);
            HBaseAdmin admin=new HBaseAdmin(conf);
            if(admin.tableExists(tableName)){
               System.out.println("表已存在，表名："+tableName); 
            }else{
               // HTableDescriptor table=new HTableDescriptor(tableName);
                HTableDescriptor table=new HTableDescriptor(TableName.valueOf(tableName));
                for(String cf:columnFamilys){
                    HColumnDescriptor col=new HColumnDescriptor(cf);
                    table.addFamily(col);
                }
                admin.createTable(table);
                System.out.println("创建表成功，name="+tableName+";cols="+columnFamilys.toString());
            }
            admin.close();
        } catch (MasterNotRunningException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //删除一张表,删除表之前需要先禁用掉表
    static void deleteTable(String tableName){
        HBaseAdmin admin;
        try {
            System.out.println("删除一张表，如果表存在则给出提示"+"start!"+tableName);
            admin = new HBaseAdmin(conf);
            if(admin.tableExists(tableName)){
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
                System.out.println("删除表成功，表名："+tableName);
            }else{
                System.out.println("表{"+tableName+"}不存在！");
            }
            admin.close();
        } catch (MasterNotRunningException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    //删除其中一条记录
    static void deleteRow(String tableName,String rowKey){
        HTable table;
        try {
            System.out.println("删除其中一条记录"+"start!"+tableName+":"+rowKey);
            table = new HTable(conf, tableName);
            Delete delete=new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
            table.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    //批量删除记录
    static void deleteRowList(String tableName,String[] rows){
        try {
            System.out.println("批量删除记录"+"start!"+tableName+":"+rows.toString());
            HTable table=new HTable(conf, tableName);
            List<Delete> deleteList=new ArrayList<Delete>();
            for(String row : rows){
                Delete del=new Delete(Bytes.toBytes(row));
                deleteList.add(del);
            }
            table.delete(deleteList);
            table.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //批量插入数据 rowList.add(new String[]{"test0922","11", "age", "12", "120"});
    static void insertRowList(String tableName,List<String[]> rowList){
        
        try {
            System.out.println("批量插入数据"+"start!"+tableName+":"+rowList.toString());
            HTable table=new HTable(conf, tableName);
            List<Put> putList=new ArrayList<Put>();
            for(String[] r:rowList){
                Put put=new Put(Bytes.toBytes(r[1]));
                put.add(Bytes.toBytes(r[2]),Bytes.toBytes(r[3]),1000,Bytes.toBytes(r[4]));
                putList.add(put);
            }
            table.put(putList);
            table.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 插入一条数据
     * @param tableName　表名
     * @param row　行号
     * @param colFamily　列簇
     * @param column　列
     * @param value　列值
     */
    static void insertRow(String tableName,String rowKey,String colFamily,String column,String value){
        try {
            System.out.println("插入一条数据"+"start!"+tableName+":"+rowKey+":"+colFamily+":"+column+":"+value);
            HTable table=new HTable(conf, tableName);
            Put put=new Put(Bytes.toBytes(rowKey));
            put.add(Bytes.toBytes(colFamily),Bytes.toBytes(column),Bytes.toBytes(value));
            table.put(put);
            table.close();
            System.out.println("插入一条数据成功！tableName="+tableName+":row="+rowKey+":colFamily="+colFamily+":column="+column+":value="+value);
                    
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //根据rowId查询数据
    static void getDataByRowId(String tableName,String rowKey) throws Exception{
        try {
            System.out.println("插入一条数据"+"start!"+tableName+":"+rowKey);
            HTable table=new HTable(conf, tableName);
            Get get = new Get(Bytes.toBytes(rowKey));
            Result result=table.get(get);
            printRecoder(result);
            table.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        /*try {
            HTable table=new HTable(conf, tableName);
            Get g=new Get(Bytes.toBytes(rowKey));
            Result rs=table.get(g);
            for(Cell k:rs.rawCells()){
                System.out.println("行号："+new String(CellUtil.cloneRow(k)));
                System.out.println("时间戳："+k.getTimestamp());
                System.out.println("列簇："+new String(CellUtil.cloneFamily(k)));
                System.out.println("列："+new String(k.getQualifier()));
                System.out.println("值："+new String(CellUtil.cloneValue(k)));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
    }
    //查询某个表下的所有数据
    static void listData(String tableName) throws Exception{
        try {
            System.out.println("查询某个表下的所有数据"+"start!"+tableName);
            HTable table=new HTable(conf,tableName);
            Scan scan=new Scan();
            ResultScanner rs=table.getScanner(scan);
            for(Result r:rs){
                printRecoder(r);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    //查看某个表下指定行的数据
    static void listDataByRowkey(String tableName,String rowKey) throws Exception{
        try {
            System.out.println("查看某个表下指定行的数据"+"start!"+tableName+":"+rowKey);
            HTable table=new HTable(conf, tableName);
            Scan scan=new Scan();
            scan.setFilter(new PrefixFilter(Bytes.toBytes(rowKey)));
            ResultScanner rs=table.getScanner(scan);
            for(Result r:rs){
                printRecoder(r);
            }
            table.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 查询某个表下的指定条数的数据
     * @param tableName　表名
     * @param rowKey　行键扫描
     * @param limit 查询的条数
     * @throws Exception
     */
    static void listDataByRowkeyAndLimit(String tableName,String rowKey,long limit) throws Exception{
        try {
            System.out.println("查询某个表下的指定条数的数据"+"start!"+tableName+":"+rowKey+":"+limit);
            HTable table=new HTable(conf, tableName);
            Scan scan=new Scan();
            scan.setFilter(new PrefixFilter(Bytes.toBytes(rowKey)));
            scan.setFilter(new PageFilter(limit));
            ResultScanner rs=table.getScanner(scan);
            for(Result r:rs){
                printRecoder(r);
            }
            table.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    /**
     * 查询指定范围内的数据
     * @param tableName 表名
     * @param startRow　开始行
     * @param stopRow　结束行
     * @throws Exception
     */
    static void scanDataByRange(String tableName,String startRow,String stopRow) throws Exception{
        try {
            System.out.println("查询指定范围内的数据"+"start!"+tableName+":"+startRow+":"+stopRow);
            HTable table=new HTable(conf, tableName);
            Scan scan=new Scan();
            scan.setStartRow(Bytes.toBytes(startRow));
            scan.setStopRow(Bytes.toBytes(stopRow));
            ResultScanner rs=table.getScanner(scan);
            for(Result r:rs){
                printRecoder(r);
            }
            table.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 获取表中某个字段的值
     * @param tableName　表名
     * @param colFamily　列簇
     * @param column　列名
     */
    static void getDetailsByValue(String tableName,String colFamily,String column){
        try {
            System.out.println("查询指定范围内的数据"+"start!"+tableName+":"+colFamily+":"+column);
            HTable table=new HTable(conf, tableName);
            Scan scan=new Scan();
            ResultScanner rs=table.getScanner(scan);
            for(Result r:rs){
                if(r.getValue(Bytes.toBytes(colFamily), Bytes.toBytes(column))!=null){
                    System.out.println("获取到值："+new String(r.getValue(Bytes.toBytes(colFamily), Bytes.toBytes(column))));
                }
            }
            table.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    /** 
     * 打印一条记录的详情 
     *  
     * */  
    public  static void printRecoder(Result result)throws Exception{  
        for(Cell cell:result.rawCells()){  
            System.out.println("行健: "+new String(CellUtil.cloneRow(cell)));  
            System.out.println("列簇: "+new String(CellUtil.cloneFamily(cell)));  
            System.out.println(" 列: "+new String(CellUtil.cloneQualifier(cell)));  
            System.out.println(" 值: "+new String(CellUtil.cloneValue(cell)));  
            System.out.println("时间戳: "+cell.getTimestamp());      
        } 
        for(KeyValue rowkv:result.raw()){
            System.out.print("Row Name: " + new String(rowkv.getRow()) + " ");
            System.out.print("Timestamp: " + rowkv.getTimestamp() + " ");
            System.out.print("column Family: " + new String(rowkv.getFamily()) + " ");
            System.out.print("Row Name:  " + new String(rowkv.getQualifier()) + " ");
            System.out.println("Value: " + new String(rowkv.getValue()) + " ");
        }
    }  
    public static void main(String[] args) throws Exception {
       createTable("test0922", new String[]{"name","age","address"});
       deleteRow("test0922", "15");
      // deleteRowList("test0922", rows);
      /* List<String[]> rowList=new ArrayList<String[]>();
       rowList.add(new String[]{"test0922","11", "age", "11", "110"});
       rowList.add(new String[]{"test0922","11", "age", "12", "120"});
       insertRowList("test0922", rowList);*/
       /*insertRow("test0922","1", "age", "basic", "10");
       insertRow("test0922","2","age","second","300");
       insertRow("test0922","3","age","three","500");
       insertRow("test0922","4","address","provine","湖北");
       insertRow("test0922","5","address","city","随州");*/
       
        //getDataByRowId("test0922", "5");
        listData("test0922");
       //listDataByRowkey("test0922", "1"); //模糊查询，能查出多条记录
        //listDataByRowkeyAndLimit("test0922", "1", 4);
        //scanDataByRange("test0922","3","5");//5-3条记录
       //insertRow("test0922","1", "age", "basic", "121");
        //getDetailsByValue("test0922", "age", "basic");
      //  deleteTable("test0922");
    }
}
