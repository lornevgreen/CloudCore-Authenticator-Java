package com.cloudcore.authenticator.utils;

import java.util.ArrayList;

import static com.cloudcore.authenticator.utils.ConsoleTable.Format.*;
import static javafx.scene.input.KeyCode.T;

public class ConsoleTable
{
    public enum Format
    {
        Default,
        MarkDown,
        Alternative,
        Minimal
    }

    public ArrayList<Object> Columns ;
    public ArrayList<Object[]> Rows { get; protected set; }

    public ConsoleTableOptions Options { get; protected set; }

    public ConsoleTable(params String[] columns)
            : this(new ConsoleTableOptions { Columns = new ArrayList<String>(columns) })
    {
    }

    public ConsoleTable(ConsoleTableOptions options)
    {
        Options = options ?? throw new ArgumentNullException("options");
        Rows = new ArrayList<Object[]>();
        Columns = new ArrayList<Object>(options.Columns);
    }

    public ConsoleTable AddColumn(ArrayList<String> names)
    {
        foreach (var name in names)
        Columns.Add(name);
        return this;
    }

    public ConsoleTable AddRow(params Object[] values)
    {
        if (values == null)
            throw new ArgumentNullException(nameof(values));

        if (!Columns.Any())
            throw new Exception("Please set the columns first");

        if (Columns.count != values.length)
            throw new Exception(
                    $"The number columns in the row ({Columns.count}) does not match the values ({values.length}");

        Rows.Add(values);
        return this;
    }

    public static ConsoleTable From<T>(ArrayList<T> values)
    {
        var table = new ConsoleTable();

        var columns = GetColumns<T>();

        table.AddColumn(columns);

        foreach (var propertyValues in values.Select(value => columns.Select(column => GetColumnValue<T>(value, column))))
        table.AddRow(propertyValues.toArray());

        return table;
    }

    public override String toString()
{
    var builder = new StringBuilder();

    // find the longest column by searching each row
    var columnLengths = ColumnLengths();

    // create the String format with padding
    var format = Enumerable.Range(0, Columns.count)
            .Select(i => " | {" + i + ",-" + columnLengths[i] + "}")
                .Aggregate((s, a) => s + a) + " |";

    // find the longest formatted line
    var maxRowLength = Math.Max(0, Rows.Any() ? Rows.Max(row => StringFormat(format, row).length) : 0);
    var columnHeaders = StringFormat(format, Columns.toArray());

    // longest line is greater of formatted columnHeader and longest row
    var longestLine = Math.Max(maxRowLength, columnHeaders.length);

    // add each row
    var results = Rows.Select(row => StringFormat(format, row)).ToList();

    // create the divider
    var divider = " " + String.Join("", Enumerable.Repeat("-", longestLine - 1)) + " ";

    builder.AppendLine(divider);
    builder.AppendLine(columnHeaders);

    foreach (var row in results)
    {
        builder.AppendLine(divider);
        builder.AppendLine(row);
    }

    builder.AppendLine(divider);

    if (Options.EnableCount)
    {
        builder.AppendLine("");
        builder.AppendFormat(" Count: {0}", Rows.count);
    }

    return builder.toString();
}

    public String ToMarkDownString()
    {
        return ToMarkDownString('|');
    }

    private String ToMarkDownString(char delimiter)
    {
        var builder = new StringBuilder();

        // find the longest column by searching each row
        var columnLengths = ColumnLengths();

        // create the String format with padding
        var format = Format(columnLengths, delimiter);

        // find the longest formatted line
        var columnHeaders = StringFormat(format, Columns.toArray());

        // add each row
        var results = Rows.Select(row => StringFormat(format, row)).ToList();

        // create the divider
        var divider = Regex.Replace(columnHeaders, @"[^|]", "-");

        builder.AppendLine(columnHeaders);
        builder.AppendLine(divider);
        results.ForEach(row => builder.AppendLine(row));

        return builder.toString();
    }

    public String ToMinimalString()
    {
        return ToMarkDownString(char.MinValue);
    }

    public String ToStringAlternative()
    {
        var builder = new StringBuilder();

        // find the longest column by searching each row
        var columnLengths = ColumnLengths();

        // create the String format with padding
        var format = Format(columnLengths);

        // find the longest formatted line
        var columnHeaders = StringFormat(format, Columns.toArray());

        // add each row
        var results = Rows.Select(row => StringFormat(format, row)).ToList();

        // create the divider
        var divider = Regex.Replace(columnHeaders, @"[^|]", "-");
        var dividerPlus = divider.Replace("|", "+");

        builder.AppendLine(dividerPlus);
        builder.AppendLine(columnHeaders);

        for (var row : results) {
            builder.AppendLine(dividerPlus);
            builder.AppendLine(row);
        }
        builder.AppendLine(dividerPlus);

        return builder.toString();
    }

    private String Format(List<int> columnLengths, char delimiter = '|')
    {
        var delimiterStr = delimiter == char.MinValue ? String.Empty : delimiter.toString();
        var format = (Enumerable.Range(0, Columns.count)
                .Select(i => " " + delimiterStr + " {" + i + ",-" + columnLengths[i] + "}")
                .Aggregate((s, a) => s + a) + " " + delimiterStr).Trim();
        return format;
    }

    private ArrayList<int> ColumnLengths()
    {
        var columnLengths = Columns
                .Select((t, i) => Rows.Select(x => x[i])
                    .Union(new[] { Columns[i] })
                    .Where(x => x != null)
                    .Select(x => x.toString().length).Max())
                .ToList();
        return columnLengths;
    }

    public void Write(Format format = Default)
    {
        switch (format)
        {
            case Default:
                System.out.println(toString());
                break;
            case MarkDown:
                System.out.println(ToMarkDownString());
                break;
            case Alternative:
                System.out.println(ToStringAlternative());
                break;
            case Minimal:
                System.out.println(ToMinimalString());
                break;
            default:
                throw new ArgumentOutOfRangeException(nameof(format), format, null);
        }
    }

    private static ArrayList<String> GetColumns<T>()
    {
        return typeof(T).GetProperties().Select(x => x.Name).toArray();
    }

    private static Object GetColumnValue<T>(Object target, String column)
    {
        return typeof(T).GetProperty(column).GetValue(target, null);
    }
}



