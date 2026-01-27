
# 导入tushare
import tushare as ts
import ZukTuShare
import pandas as pd

# 初始化pro接口
pro = ZukTuShare.getPro_10000()

# 游资交易每日明细
def download_hm_detail(trade_date):
    # 拉取数据
    df = pro.hm_detail(**{
        "trade_date": trade_date,
        "ts_code": "",
        "hm_name": "",
        "start_date": "",
        "end_date": "",
        "limit": "",
        "offset": ""
    }, fields=[
        "trade_date",
        "ts_code",
        "ts_name",
        "buy_amount",
        "sell_amount",
        "net_amount",
        "hm_name",
        "hm_orgs",
        "tag"
    ])
    # print(df)
    print(len(df))
    return df

if __name__ == '__main__':
    print("游资交易每日明细")
    trade_date = ZukTuShare.analysis_trade_date
    df = download_hm_detail(trade_date)
    path = f"hm_detail/hm_detail-{trade_date}.csv"
    print(path)
    df.to_csv(f"{path}", encoding="utf-8", index=False)
    print("完成")
