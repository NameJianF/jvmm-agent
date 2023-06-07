package live.itrip.jvmm.agent.core.entity.info;

import live.itrip.jvmm.agent.common.JsonParsable;

/**
 * description: JVM 内存管理信息
 * date 15:32 2021/5/11
 *
 * @author fengjianfeng
 */
public class JvmMemoryManagerInfo implements JsonParsable {
    private String name;
    private boolean valid;
    private String[] memoryPoolNames;

    private JvmMemoryManagerInfo(){
    }

    public static JvmMemoryManagerInfo create(){
        return new JvmMemoryManagerInfo();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String[] getMemoryPoolNames() {
        return memoryPoolNames;
    }

    public void setMemoryPoolNames(String[] memoryPoolNames) {
        this.memoryPoolNames = memoryPoolNames;
    }

    @Override
    public String toString() {
        return toJsonStr();
    }
}