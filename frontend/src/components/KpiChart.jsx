import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip
} from 'recharts'

function KpiChart({ data }) {

    return (

        <LineChart
            width={900}
            height={300}
            data={data}
        >

            <CartesianGrid strokeDasharray="3 3" />

            <XAxis dataKey="createdAt" />

            <YAxis />

            <Tooltip />

            <Line
                type="monotone"
                dataKey="value"
            />

        </LineChart>
    )
}

export default KpiChart