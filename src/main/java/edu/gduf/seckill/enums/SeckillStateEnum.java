package edu.gduf.seckill.enums;

/** 使用枚举表数常量数据字段
 * Created by zhang on 2017/4/3.
 */
public enum SeckillStateEnum {

    SUCCESS(1,"秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1, "秒杀重复"),
    INNER_ERROR(-2, "系统异常"),
    DATA_REWRITE(-3,"数据篡改");

    private int state;
    private String stateInfo;

    SeckillStateEnum(int state, String sateInfo) {
        this.state = state;
        this.stateInfo = sateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public static SeckillStateEnum StateOf(int index) {
        for(SeckillStateEnum state : values()) {
            if(state.getState() == index) {
                return  state;
            }
        }
        return null;
    }
}
