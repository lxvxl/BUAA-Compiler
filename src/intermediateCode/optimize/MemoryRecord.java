package intermediateCode.optimize;

import intermediateCode.Inst;
import intermediateCode.instructions.LoadInst;
import intermediateCode.instructions.StoreInst;

import java.util.*;

public class MemoryRecord {
    private List<AddrRecord> addrRecords = new ArrayList<>();

    /**
     * 记录一次存储操作
     */
    public void recordStore(StoreInst storeInst) {
        AddrRecord addrRecord = new AddrRecord(storeInst.addr(),
                storeInst.offset(),
                storeInst.isArray() ? storeInst.arrName() : storeInst.addr(),
                storeInst.val());
        addrRecords.removeIf(addrRecord::isConflict);
        addrRecords.add(addrRecord);
    }

    /**
     * 查看这次的读操作是否是已经读过的，如果是，返回对应的值。否则，返回null;
     */
    public String getLoadValue(LoadInst loadInst) {
        AddrRecord addrRecord = addrRecords.stream()
                .filter(r -> r.isSameLoc(loadInst.addr(), loadInst.offset()))
                .findFirst()
                .orElse(null);
        if (addrRecord == null) {
            addrRecord = new AddrRecord(loadInst.addr(),
                    loadInst.offset(),
                    loadInst.isArray() ? loadInst.arrName() : loadInst.addr(),
                    loadInst.result());
            addrRecords.add(addrRecord);
            return loadInst.result();
        } else {
            return addrRecord.value();
        }
    }

    /**
     * 处理函数的修改的内存区域
     */
    public void removeGlobalAndDirtyArea(Set<String> areaSet) {
        addrRecords.removeIf(r -> areaSet.contains(r.areaName) || Inst.isGlobalParam(r.areaName));
    }

    public boolean hasSideEffect(String areaName) {
        return this.addrRecords.stream().anyMatch(r -> r.areaName.equals(areaName));
    }
    //TODO 函数传参传进去的地址是否改变还又没搞定
    public void deleteAreaName(String areaName) {
        this.addrRecords.removeIf(r -> r.areaName.equals(areaName));
    }

    private record AddrRecord(String addrReg, int off, String areaName, String value) {
        public boolean isConflict(AddrRecord another) {
            if (!this.areaName.equals(another.areaName)) {
                return false;
            }
            if (!this.addrReg.equals(areaName) || !another.addrReg.equals(areaName)) {
                return false;
            }
            return this.off == another.off;
        }

        public boolean isSameLoc(String addrReg, int off) {
            return this.addrReg.equals(addrReg)
                    && this.off == off;
        }
    }
}
