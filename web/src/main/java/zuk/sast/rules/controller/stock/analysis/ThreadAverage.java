package zuk.sast.rules.controller.stock.analysis;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/***
 * 均线计算
 */
@Slf4j
public class ThreadAverage implements Runnable{

    private List<String> codes;

    public ThreadAverage(List<String> codes) {
        this.codes = codes;
    }

    @Override
    public void run() {
        
    }

}
