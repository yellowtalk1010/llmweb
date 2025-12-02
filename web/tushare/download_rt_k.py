import tushare as ts

import ZukTuShare
# 初始化pro接口
pro = ts.pro_api(ZukTuShare.token)

# 拉取数据
def rt_k():
    df = pro.rt_k(**{
        "topic": "",
        "ts_code": "0*.SZ,3*.SZ,6*.SH,9*.BJ",
        "limit": "",
        "offset": ""
    }, fields=[
        "ts_code",
        "name",
        "pre_close",
        "high",
        "open",
        "low",
        "close",
        "vol",
        "amount",
        "num"
    ])
    print(df)
    return df

# 每天最多访问该接口2次，
if __name__ == '__main__':
    df = rt_k()
    df.to_csv('rt_k/rt_k.csv', index=False)
    print("完成")

        
