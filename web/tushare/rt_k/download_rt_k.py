import tushare as ts

import ZukTuShare
# 初始化pro接口
pro = ts.pro_api("d6a7b03012743e8b035a4c37ec258b77fb5a65500c0629e0022cffde")

# pro = ts.pro_api("c1r591l1j03n22x258")
# pro._DataApi__token = 'c1r591l1j03n22x258'
# pro._DataApi__http_url = 'http://proplus.tushare.nlink.vip'

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
    # print(df)
    return df

# 每天最多访问该接口2次，每小时1次
if __name__ == '__main__':
    df = rt_k()
    df.to_csv('rt_k.csv', encoding="utf-8", index=False)
    print("完成")

        
