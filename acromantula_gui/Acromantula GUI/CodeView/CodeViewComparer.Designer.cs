namespace Acromantula_GUI.CodeView
{
    partial class CodeViewComparer
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(CodeViewComparer));
            this.scintillaDiffControl1 = new ScintillaDiff.ScintillaDiffControl();
            this.SuspendLayout();
            // 
            // scintillaDiffControl1
            // 
            this.scintillaDiffControl1.AddedCharacterSymbol = '+';
            this.scintillaDiffControl1.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.scintillaDiffControl1.CharacterComparison = true;
            this.scintillaDiffControl1.CharacterComparisonMarkAddRemove = true;
            this.scintillaDiffControl1.DiffColorAdded = System.Drawing.Color.FromArgb(((int)(((byte)(212)))), ((int)(((byte)(242)))), ((int)(((byte)(196)))));
            this.scintillaDiffControl1.DiffColorChangeBackground = System.Drawing.Color.FromArgb(((int)(((byte)(252)))), ((int)(((byte)(255)))), ((int)(((byte)(140)))));
            this.scintillaDiffControl1.DiffColorCharAdded = System.Drawing.Color.FromArgb(((int)(((byte)(154)))), ((int)(((byte)(234)))), ((int)(((byte)(111)))));
            this.scintillaDiffControl1.DiffColorCharDeleted = System.Drawing.Color.FromArgb(((int)(((byte)(225)))), ((int)(((byte)(125)))), ((int)(((byte)(125)))));
            this.scintillaDiffControl1.DiffColorDeleted = System.Drawing.Color.FromArgb(((int)(((byte)(255)))), ((int)(((byte)(178)))), ((int)(((byte)(178)))));
            this.scintillaDiffControl1.DiffStyle = ScintillaDiff.ScintillaDiffStyles.DiffStyle.DiffList;
            this.scintillaDiffControl1.ImageRowAdded = ((System.Drawing.Bitmap)(resources.GetObject("scintillaDiffControl1.ImageRowAdded")));
            this.scintillaDiffControl1.ImageRowAddedScintillaIndex = 28;
            this.scintillaDiffControl1.ImageRowDeleted = ((System.Drawing.Bitmap)(resources.GetObject("scintillaDiffControl1.ImageRowDeleted")));
            this.scintillaDiffControl1.ImageRowDeletedScintillaIndex = 29;
            this.scintillaDiffControl1.ImageRowDiff = ((System.Drawing.Bitmap)(resources.GetObject("scintillaDiffControl1.ImageRowDiff")));
            this.scintillaDiffControl1.ImageRowDiffScintillaIndex = 31;
            this.scintillaDiffControl1.ImageRowOk = ((System.Drawing.Bitmap)(resources.GetObject("scintillaDiffControl1.ImageRowOk")));
            this.scintillaDiffControl1.ImageRowOkScintillaIndex = 30;
            this.scintillaDiffControl1.IsEntireLineHighlighted = false;
            this.scintillaDiffControl1.Location = new System.Drawing.Point(0, 0);
            this.scintillaDiffControl1.MarkColorIndexModifiedBackground = 31;
            this.scintillaDiffControl1.MarkColorIndexRemovedOrAdded = 30;
            this.scintillaDiffControl1.Name = "scintillaDiffControl1";
            this.scintillaDiffControl1.RemovedCharacterSymbol = '-';
            this.scintillaDiffControl1.Size = new System.Drawing.Size(800, 449);
            this.scintillaDiffControl1.TabIndex = 0;
            this.scintillaDiffControl1.TextLeft = "";
            this.scintillaDiffControl1.TextRight = "";
            this.scintillaDiffControl1.UseRowOkSign = true;
            this.scintillaDiffControl1.Load += new System.EventHandler(this.scintillaDiffControl1_Load);
            // 
            // CodeViewComparer
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(800, 450);
            this.Controls.Add(this.scintillaDiffControl1);
            this.Name = "CodeViewComparer";
            this.Text = "CodeViewComparer";
            this.Load += new System.EventHandler(this.CodeViewComparer_Load);
            this.ResumeLayout(false);

        }

        #endregion

        private ScintillaDiff.ScintillaDiffControl scintillaDiffControl1;
    }
}