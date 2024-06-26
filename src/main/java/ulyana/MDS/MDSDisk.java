package ulyana.MDS;

import ulyana.OSD.*;
import ulyana.Monitor.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;

//операции если mds хранится на OSD
//после каждого изменения пересохраняется на диски
public class MDSDisk {
    private MDS mds;
    final private DataOperation osd;
    final private MonitorOperation monitor;
    final private String inodeNumber = "0";//нужен для сохранения на osd блока с размером файла
    final private int sizeBlock = 4096;

    public MDSDisk(DataOperation osd, MonitorOperation monitor) throws Exception {
        this.osd = osd;
        this.monitor = monitor;
        loadFromOSD();
    }

    public Object addInodeFile(String nameInode, int size, int countBlock, Date date) throws Exception {
        Object result = mds.addInodeFile(nameInode, size, countBlock, date);
        if (result instanceof Integer) {
            MDSSaveToOSD save = new MDSSaveToOSD(mds, osd, monitor);
            save.start();
            save.join();
        }
        return result;
    }

    public Object addInodeDirectory(String nameInode) throws Exception {
        Object result = mds.addInodeDirectory(nameInode);
        if (result instanceof Integer) {
            MDSSaveToOSD save = new MDSSaveToOSD(mds, osd, monitor);
            save.start();
            save.join();
        }
        return result;
    }

    public Object removeFile(String name, String nameInode) throws Exception {
        Object result = mds.removeFile(name, nameInode);
        MDSSaveToOSD save = new MDSSaveToOSD(mds, osd, monitor);
        save.start();
        save.join();
        return result;
    }

    public Object removeDirectory(String name, String nameInode) throws Exception {
        Object result = mds.removeDirectory(name, nameInode);
        MDSSaveToOSD save = new MDSSaveToOSD(mds, osd, monitor);
        save.start();
        save.join();
        return result;
    }

    public InodeFile find(String nameInode) throws Exception {
        InodeFile result = mds.find(nameInode);
        if (result != null) return new InodeFile(result);
        return null;
    }

    public String ls(String nameInode) {
        return mds.ls(nameInode);
    }

    public Object update(String nameUser, String nameInode, int size, int countBlock, Date date) throws Exception {
        Object result = mds.updateFile(nameUser, nameInode, size, countBlock, date);
        MDSSaveToOSD save = new MDSSaveToOSD(mds, osd, monitor);
        save.start();
        save.join();
        return result;
    }

    public Object blockFile(String nameUser, String nameInode, Date date) throws Exception {
        Object result = mds.blockFile(nameUser, nameInode, date);
        MDSSaveToOSD save = new MDSSaveToOSD(mds, osd, monitor);
        save.start();
        save.join();
        return result;
    }

    public Object unblockFile(String nameUser, String nameInode, Date date) throws Exception {
        Object result = mds.unblockFile(nameUser, nameInode, date);
        MDSSaveToOSD save = new MDSSaveToOSD(mds, osd, monitor);
        save.start();
        save.join();
        return result;
    }

    public boolean cd(String nameInode) throws Exception {
        boolean result = mds.cd(nameInode);
        if (result) {
            MDSSaveToOSD save = new MDSSaveToOSD(mds, osd, monitor);
            save.start();
            save.join();
        }
        return result;
    }

    public String pwd() {
        return mds.pwd();
    }

    //загрузить mds из osd если было сохранение или создать mds если до этого на диске не хранился
    public void loadFromOSD() throws Exception {
        CalculateOSD calculateOSD = new CalculateOSD(monitor);
        ArrayList<DiskBucket> osds = calculateOSD.getOSDs(inodeNumber);
        Block block = null;
        for(DiskBucket disk:osds) {
            block = osd.get(disk, inodeNumber);
            if (block != null) break;
        }
        //если такой блок не нашли значит такого сохранения не было
        if (block != null) {
            String sizeString = new String(block.getData());
            int size = Integer.parseInt(sizeString);
            int countBlock = (size / sizeBlock) + 1;
            byte[] mdsByte = new byte[size];//результирующий массив с mds
            for (int i = 0; i < countBlock; i++) {
                String idBlock = inodeNumber.concat(".") + i;
                osds = calculateOSD.getOSDs(inodeNumber);
                block = null;
                for(DiskBucket disk:osds) {
                    block = osd.get(disk, idBlock);
                    if (block != null) break;
                }
                if (block == null)
                    throw new Exception("Load mds failure");
                System.arraycopy(block.getData(), 0, mdsByte, i * sizeBlock, block.getData().length);
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(mdsByte);
            ObjectInputStream in = new ObjectInputStream(inputStream);
            mds = (MDS) in.readObject();
            in.close();
            inputStream.close();
        }
        else {
            mds = new MDS();
        }
    }

    public MDS getMDS(){
        return mds;
    }

    public InodeDirectory getRoot(){
        return mds.getRootInode();
    }
}
