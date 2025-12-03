
import tushare as ts

import ZukTuShare
# 初始化pro接口
pro = ts.pro_api(ZukTuShare.token)

if __name__ == '__main__':
    df = pro.rt_min(ts_code='600000.SH,000001.SZ', freq='1MIN')
    print(df)