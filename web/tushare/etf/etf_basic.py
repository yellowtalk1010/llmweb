
# 导入tushare
import tushare as ts
import ZukTuShare

# 初始化pro接口
pro = ZukTuShare.getPro()

def download_etf_basic():
    # 拉取数据
    df = pro.etf_basic(**{
        "ts_code": "",
        "index_code": "",
        "list_date": "",
        "list_status": "",
        "exchange": "",
        "mgr": "",
        "limit": "",
        "offset": ""
    }, fields=[
        "ts_code",
        "csname",
        "extname",
        "cname",
        "index_code",
        "index_name",
        "setup_date",
        "list_date",
        "list_status",
        "exchange",
        "mgr_name",
        "custod_name",
        "mgt_fee",
        "etf_type"
    ])
    # print(df)
    return df


if __name__ == '__main__':
    print("ETF基本信息")
    df = download_etf_basic()
    df.to_csv("etf_basic.csv", encoding="utf-8", index=False)
    print("完成")
