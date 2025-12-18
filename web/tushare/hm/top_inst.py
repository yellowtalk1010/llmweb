# 导入tushare
import tushare as ts
import pandas as pd
import ZukTuShare

# 初始化pro接口
pro = ZukTuShare.getPro_5000()

# 拉取数据
def top_inst(trade_date):
    df = pro.top_inst(**{
        "trade_date": trade_date,
        "ts_code": "",
        "limit": "",
        "offset": ""
    }, fields=[
        "trade_date",
        "ts_code",
        "exalter",
        "buy",
        "buy_rate",
        "sell",
        "sell_rate",
        "net_buy",
        "side",
        "reason"
    ])
    # print(df)
    return df


if __name__ == '__main__':
    trade_date = "20251218"
    df = top_inst(trade_date)
    df.to_csv(f"top_inst/{trade_date}_top_inst.csv", encoding="utf-8", index=False)
    print("完成")