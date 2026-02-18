const inputSanitizer = (req, res, next) => {
    if (req.body) {
        for (const key in req.body) {
            if (typeof req.body[key] === 'string') {
                // Simple regex to remove <script> tags
                req.body[key] = req.body[key].replace(/<script\b[^>]*>([\s\S]*?)<\/script>/gim, "");
            }
        }
    }
    next();
};

module.exports = { inputSanitizer };
