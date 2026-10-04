import http from "node:http";

const port = Number(process.env.PORT || 10000);

const send = (res, status, body) => {
  res.writeHead(status, {"Content-Type":"application/json; charset=utf-8","Access-Control-Allow-Origin":"*","Cache-Control":"no-store"});
  res.end(JSON.stringify(body));
};

const server = http.createServer((req,res)=>{
  if(req.method==="OPTIONS"){
    res.writeHead(204,{"Access-Control-Allow-Origin":"*","Access-Control-Allow-Methods":"GET,OPTIONS","Access-Control-Allow-Headers":"Content-Type"});
    return res.end();
  }
  if(req.url==="/health" || req.url==="/api/health"){
    return send(res,200,{ok:true,service:"NotifyVault API",privacy:"local-first",time:new Date().toISOString()});
  }
  if(req.url==="/api"){
    return send(res,200,{name:"NotifyVault API",version:"1.0.0",endpoints:["GET /health","GET /api"]});
  }
  return send(res,404,{error:"not_found"});
});

server.listen(port,"0.0.0.0",()=>console.log("NotifyVault API listening on "+port));
