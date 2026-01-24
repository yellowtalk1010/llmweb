from tushare.stock.cons import ALL_STOCK_BASICS_FILE

import ZukTuShare

# 导入tushare
import tushare as ts
import pandas as pd

ALL_STOCK_BASICS_FILE = "all_stocks.csv"  #全部股票数据存放路径

# 位置：数据接口->股票数据->基础数据->股票列表
def down_load_stock_basic():

    # 初始化pro接口
    pro = ts.pro_api(ZukTuShare.getPro_10000())

    # 拉取全部股票数据
    df = pro.stock_basic(**{
        "ts_code": "",
        "name": "",
        "exchange": "",
        "market": "",
        "is_hs": "",
        "list_status": "",
        "limit": "",
        "offset": ""
    }, fields=[
        "ts_code",
        "symbol",
        "name",
        "area",
        "industry",
        "cnspell",
        "market",
        "list_date",
        "act_name",
        "act_ent_type"
    ])
    print(df)
    return df



if __name__ == '__main__':
    print("拉取全部股票数据")
    df = down_load_stock_basic()
    print("将股票存入" + ALL_STOCK_BASICS_FILE + "文件中")
    df.to_csv(ALL_STOCK_BASICS_FILE, index=False)
