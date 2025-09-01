import React, { useState, useEffect } from 'react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, AreaChart, Area, ReferenceLine
} from 'recharts';

// 模拟股票数据生成
const generateStockData = (type) => {
  const data = [];
  const now = new Date();
  let basePrice = 150 + Math.random() * 50;
  
  if (type === 'intraday') {
    // 生成分时数据（每5分钟一个点）
    for (let i = 0; i < 78; i++) {
      const time = new Date(now);
      time.setHours(9, 30 + i * 5, 0);
      
      if (time.getHours() >= 11 && time.getHours() < 13) {
        // 午间休市，价格不变
        data.push({
          time: time.toTimeString().substr(0, 5),
          price: basePrice,
          volume: Math.floor(Math.random() * 1000)
        });
      } else {
        basePrice += (Math.random() - 0.5) * 2;
        data.push({
          time: time.toTimeString().substr(0, 5),
          price: parseFloat(basePrice.toFixed(2)),
          volume: Math.floor(Math.random() * 1000)
        });
      }
    }
  } else if (type === 'daily') {
    // 生成日K数据
    for (let i = 30; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(now.getDate() - i);
      
      const open = basePrice;
      basePrice += (Math.random() - 0.5) * 10;
      const close = basePrice;
      const high = Math.max(open, close) + Math.random() * 5;
      const low = Math.min(open, close) - Math.random() * 5;
      
      data.push({
        date: date.toLocaleDateString().substr(5),
        open: parseFloat(open.toFixed(2)),
        close: parseFloat(close.toFixed(2)),
        high: parseFloat(high.toFixed(2)),
        low: parseFloat(low.toFixed(2)),
        volume: Math.floor(Math.random() * 10000)
      });
    }
  } else if (type === '5days') {
    // 生成5日数据
    for (let i = 120; i >= 0; i--) {
      const time = new Date(now);
      time.setHours(9, 30 + i * 30, 0);
      
      if (time.getHours() >= 11 && time.getHours() < 13) {
        // 午间休市，价格不变
        data.push({
          time: time.toTimeString().substr(0, 5),
          price: basePrice,
          volume: Math.floor(Math.random() * 1000)
        });
      } else {
        basePrice += (Math.random() - 0.5);
        data.push({
          time: time.toTimeString().substr(0, 5),
          price: parseFloat(basePrice.toFixed(2)),
          volume: Math.floor(Math.random() * 1000)
        });
      }
    }
  }
  
  return data;
};

// 自定义Tooltip组件
const CustomTooltip = ({ active, payload, label }) => {
  if (active && payload && payload.length) {
    return (
      <div className="custom-tooltip">
        <p className="label">{`时间: ${label}`}</p>
        <p className="intro" style={{color: '#8884d8'}}>
          价格: {payload[0].value}
        </p>
        {payload[1] && (
          <p className="intro" style={{color: '#82ca9d'}}>
            成交量: {payload[1].value.toLocaleString()}
          </p>
        )}
      </div>
    );
  }
  return null;
};

// 主组件
const StockChart = () => {
  const [chartType, setChartType] = useState('intraday');
  const [stockData, setStockData] = useState([]);
  const [stockInfo, setStockInfo] = useState({
    name: '腾讯控股',
    code: '00700',
    price: 0,
    change: 0,
    changePercent: 0
  });

  useEffect(() => {
    // 模拟数据加载
    const data = generateStockData(chartType);
    setStockData(data);
    
    // 更新股票信息
    if (data.length > 0) {
      const latestPrice = data[data.length - 1].price || data[data.length - 1].close;
      const prevPrice = data[data.length - 2].price || data[data.length - 2].close;
      const change = latestPrice - prevPrice;
      const changePercent = (change / prevPrice) * 100;
      
      setStockInfo({
        ...stockInfo,
        price: latestPrice,
        change: change,
        changePercent: changePercent
      });
    }
  }, [chartType]);

  const renderChart = () => {
    if (chartType === 'intraday' || chartType === '5days') {
      return (
        <AreaChart
          data={stockData}
          margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
        >
          <defs>
            <linearGradient id="colorPrice" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#8884d8" stopOpacity={0.8}/>
              <stop offset="95%" stopColor="#8884d8" stopOpacity={0}/>
            </linearGradient>
          </defs>
          <XAxis dataKey="time" />
          <YAxis domain={['dataMin - 1', 'dataMax + 1']} />
          <CartesianGrid strokeDasharray="3 3" />
          <Tooltip content={<CustomTooltip />} />
          <Area
            type="monotone"
            dataKey="price"
            stroke="#8884d8"
            fillOpacity={1}
            fill="url(#colorPrice)"
          />
          <ReferenceLine
            y={stockInfo.price}
            stroke={stockInfo.change >= 0 ? 'green' : 'red'}
            strokeDasharray="3 3"
          />
        </AreaChart>
      );
    } else if (chartType === 'daily') {
      return (
        <LineChart
          data={stockData}
          margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
        >
          <XAxis dataKey="date" />
          <YAxis domain={['dataMin - 1', 'dataMax + 1']} />
          <CartesianGrid strokeDasharray="3 3" />
          <Tooltip content={<CustomTooltip />} />
          <Line type="monotone" dataKey="close" stroke="#8884d8" dot={false} />
        </LineChart>
      );
    }
  };

  return (
    <div className="stock-chart-container">
      <div className="stock-header">
        <div className="stock-info">
          <h2>{stockInfo.name} ({stockInfo.code})</h2>
          <div className="price-display">
            <span className="price">{stockInfo.price.toFixed(2)}</span>
            <span className={`change ${stockInfo.change >= 0 ? 'positive' : 'negative'}`}>
              {stockInfo.change >= 0 ? '+' : ''}{stockInfo.change.toFixed(2)} ({stockInfo.change >= 0 ? '+' : ''}{stockInfo.changePercent.toFixed(2)}%)
            </span>
          </div>
        </div>
        <div className="chart-controls">
          <button
            className={chartType === 'intraday' ? 'active' : ''}
            onClick={() => setChartType('intraday')}
          >
            分时
          </button>
          <button
            className={chartType === 'daily' ? 'active' : ''}
            onClick={() => setChartType('daily')}
          >
            日K
          </button>
          <button
            className={chartType === '5days' ? 'active' : ''}
            onClick={() => setChartType('5days')}
          >
            5日
          </button>
        </div>
      </div>
      
      <div className="chart-area">
        <ResponsiveContainer width="100%" height={400}>
          {renderChart()}
        </ResponsiveContainer>
      </div>
      
      <style jsx>{`
        .stock-chart-container {
          font-family: 'Arial', sans-serif;
          background: #1e1f26;
          color: #e0e0e0;
          border-radius: 10px;
          padding: 20px;
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
          max-width: 900px;
          margin: 0 auto;
        }
        
        .stock-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
          padding-bottom: 15px;
          border-bottom: 1px solid #333;
        }
        
        .stock-info h2 {
          margin: 0 0 10px 0;
          font-size: 1.5rem;
        }
        
        .price-display {
          display: flex;
          align-items: center;
        }
        
        .price {
          font-size: 2rem;
          font-weight: bold;
          margin-right: 15px;
        }
        
        .change {
          font-size: 1.2rem;
          font-weight: bold;
        }
        
        .change.positive {
          color: #00b16a;
        }
        
        .change.negative {
          color: #ff6b6b;
        }
        
        .chart-controls {
          display: flex;
          gap: 10px;
        }
        
        .chart-controls button {
          background: #333;
          color: #e0e0e0;
          border: none;
          padding: 8px 16px;
          border-radius: 5px;
          cursor: pointer;
          transition: background 0.3s;
        }
        
        .chart-controls button:hover {
          background: #444;
        }
        
        .chart-controls button.active {
          background: #555;
          font-weight: bold;
        }
        
        .chart-area {
          margin-top: 20px;
        }
        
        .custom-tooltip {
          background: #2c2d33;
          border: 1px solid #444;
          padding: 10px;
          border-radius: 5px;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
        }
        
        .custom-tooltip .label {
          margin: 0;
          font-weight: bold;
        }
        
        .custom-tooltip .intro {
          margin: 5px 0 0 0;
        }
      `}</style>
    </div>
  );
};

export default StockChart;