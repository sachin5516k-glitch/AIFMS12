const Sales = require('../sales/salesModel');
const Inventory = require('../inventory/inventoryModel');
const Attendance = require('../attendance/attendanceModel');

const checkCompliance = async (outletId) => {
    const violations = [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // 1. Check Sales Submission
    const salesCount = await Sales.countDocuments({
        outletId,
        createdAt: { $gte: today }
    });
    if (salesCount === 0) violations.push('Missing Daily Sales Report');

    // 2. Check Inventory Submission
    const inventoryCount = await Inventory.countDocuments({
        outletId,
        createdAt: { $gte: today }
    });
    if (inventoryCount === 0) violations.push('Missing Inventory Update');

    // 3. Check Attendance (All staff)
    // Simplified: Check if *any* attendance exists for this outlet
    const attendanceCount = await Attendance.countDocuments({
        outletId,
        createdAt: { $gte: today }
    });
    if (attendanceCount === 0) violations.push('No Staff Attendance Marked');

    // Log violations (In prod: Save to ComplianceLog collection)
    if (violations.length > 0) {
        console.log(`Compliance Violations for ${outletId}:`, violations);
    }

    return violations;
};

module.exports = { checkCompliance };
