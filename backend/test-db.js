const mongoose = require('mongoose');
const uri = 'mongodb://Sachin:sachin123@ac-q5yn0lc-shard-00-00.bf10lo2.mongodb.net:27017,ac-q5yn0lc-shard-00-01.bf10lo2.mongodb.net:27017,ac-q5yn0lc-shard-00-02.bf10lo2.mongodb.net:27017/ai_franchise?ssl=true&replicaSet=atlas-2yts8n-shard-0&authSource=admin&retryWrites=true&w=majority';
mongoose.connect(uri).then(() => {
    console.log('Connected! IP Whitelist is active.');
    process.exit(0);
}).catch(err => {
    console.error('Connection failed:', err);
    process.exit(1);
});
